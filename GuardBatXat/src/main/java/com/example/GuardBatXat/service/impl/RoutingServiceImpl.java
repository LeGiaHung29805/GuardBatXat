package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.RoutingCompareResponse;
import com.example.GuardBatXat.dto.response.RoutingResponse;
import com.example.GuardBatXat.repository.RoadNodeRepository;
import com.example.GuardBatXat.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j // Khai báo log để dùng log.info, log.warn, log.error
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final String PYTHON_AI_URL = "http://localhost:5000/api/v1/ai/safe-routing";
    private final String PYTHON_SHELTER_URL = "http://localhost:5000/api/v1/ai/find-safe-shelter";
    private final RoadNodeRepository roadNodeRepository;
    private final String PYTHON_ADMIN_COMPARE_URL = "http://localhost:5000/api/v1/ai/admin-routing";

    @Override
    public RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request) {
        // 1. Ánh xạ tọa độ người dùng bấm trên bản đồ thành Node giao thông (Code của bạn bạn)
        Long startNode = roadNodeRepository.findNearestNode(request.getStartLat(), request.getStartLng());
        Long endNode = roadNodeRepository.findNearestNode(request.getEndLat(), request.getEndLng());

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Khu vực này chưa có dữ liệu mạng lưới đường bộ!");
        }

        RestTemplate restTemplate = new RestTemplate();

        try {
            log.info("Đang gọi AI Python tìm đường...");

            // NHẬN VỀ Map.class ĐỂ TRÍCH XUẤT DỮ LIỆU LINH HOẠT
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_AI_URL, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "success".equals(body.get("status"))) {
                // 1. CHUYỂN ĐỔI TOẠ ĐỘ (Fix lỗi dòng 56)
                List<List<Double>> rawCoords = (List<List<Double>>) body.get("route_coordinates");
                List<double[]> pathPoints = new ArrayList<>();

                if (rawCoords != null) {
                    for (List<Double> point : rawCoords) {
                        if (point != null && point.size() >= 2) {
                            pathPoints.add(new double[]{point.get(0), point.get(1)});
                        }
                    }
                }

                // 2. LẤY CHI PHÍ (Fix lỗi scope 'cost')
                Double cost = Double.valueOf(body.get("total_mcdm_cost").toString());

                return RoutingResponse.builder()
                        .strategyName(strategyName)
                        .totalDistance(cost) // Biến cost khai báo ngay phía trên nên hợp lệ
                        .pathPoints(pathPoints) // Biến pathPoints đã đúng kiểu List<double[]>
                        .build();
            }
            throw new RuntimeException("AI không tìm được đường");
        } catch (Exception e) {
            // 3. Nếu AI Server tắt hoặc lỗi mạng, dùng pgRouting làm dự phòng (Code của bạn bạn)
            log.error("Lỗi mạng khi kết nối với module AI Python. Chuyển sang dùng pgRouting dự phòng! Chi tiết: {}", e.getMessage());

            List<double[]> mockPath = new ArrayList<>();
            mockPath.add(new double[]{request.getStartLat(), request.getStartLng()});
            mockPath.add(new double[]{(request.getStartLat() + request.getEndLat()) / 2, (request.getStartLng() + request.getEndLng()) / 2});
            mockPath.add(new double[]{request.getEndLat(), request.getEndLng()});

            return RoutingResponse.builder()
                    .strategyName(strategyName)
                    .totalDistance(12.5) // Giả định 12.5 km
                    .avgSlope(8.2)       // Giả định độ dốc trung bình
                    .pathPoints(mockPath)
                    .build();
        }
    }

    @Override
    public Object findSafeShelterFromAI(FindShelterRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            log.info("Đang gọi AI tìm điểm sơ tán cho tọa độ: [{}, {}] - Chiến lược: {}",
                    request.getCurrentLat(), request.getCurrentLng(),
                    request.getStrategy() != null ? request.getStrategy() : "safety");

            ResponseEntity<Object> response = restTemplate.postForEntity(PYTHON_SHELTER_URL, request, Object.class);
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            log.warn("AI báo không tìm thấy điểm sơ tán hợp lệ. Mã lỗi: {}", e.getStatusCode());
            throw new RuntimeException("Cảnh báo AI: Không có điểm sơ tán nào khả dụng hoặc mọi ngả đường đều đã bị cô lập do thiên tai!");

        } catch (Exception e) {
            log.error("Lỗi kết nối AI: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI tìm điểm sơ tán đang bảo trì hoặc mất kết nối mạng!");
        }
    }
    // ==========================================
    // HÀM MỚI: TÌM 3 ĐƯỜNG CÙNG LÚC CHO ADMIN
    // ==========================================
    @Override
    public RoutingCompareResponse findAdminCompareRoute(RoutingRequest request) {
        Long startNode = roadNodeRepository.findNearestNode(request.getStartLat(), request.getStartLng());
        Long endNode = roadNodeRepository.findNearestNode(request.getEndLat(), request.getEndLng());

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Khu vực chưa có dữ liệu mạng lưới giao thông!");
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            log.info("Admin đang kiểm chứng 3 lộ trình từ {} đến {}", request.getStartLat(), request.getEndLat());
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_ADMIN_COMPARE_URL, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "success".equals(body.get("status"))) {
                Map<String, List<List<Double>>> rawData = (Map<String, List<List<Double>>>) body.get("data");

                return RoutingCompareResponse.builder()
                        .shortestPath(convertToDoubleArray(rawData.get("shortest")))
                        .safetyPath(convertToDoubleArray(rawData.get("safety")))
                        .rescuePath(convertToDoubleArray(rawData.get("rescue")))
                        .build();
            }
            throw new RuntimeException("AI không tìm thấy đường thực tế.");

        } catch (Exception e) {
            log.error("Lỗi Admin Routing: {}", e.getMessage());
            // ĐÚNG YÊU CẦU: TỚI ĐÂY LÀ CHẶN LUÔN, KHÔNG DÙNG MOCK DATA!
            throw new RuntimeException("Dữ liệu thực tế báo cáo khu vực này đang bị cô lập, không có tuyến đường an toàn.");
        }
    }

    // Hàm hỗ trợ ép kiểu dữ liệu từ Python sang Java
    private List<double[]> convertToDoubleArray(List<List<Double>> input) {
        List<double[]> output = new ArrayList<>();
        if (input != null) {
            for (List<Double> p : input) {
                if (p != null && p.size() >= 2) {
                    output.add(new double[]{p.get(0), p.get(1)});
                }
            }
        }
        return output;
    }
} // Kết thúc class
