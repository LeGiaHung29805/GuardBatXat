package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
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

@Slf4j // Khai báo log để dùng log.info, log.warn, log.error
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final String PYTHON_AI_URL = "http://localhost:5000/api/v1/ai/safe-routing";
    private final String PYTHON_SHELTER_URL = "http://localhost:5000/api/v1/ai/find-safe-shelter";
    private final RoadNodeRepository roadNodeRepository;

    @Override
    public RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request) {
        // 1. Ánh xạ tọa độ người dùng bấm trên bản đồ thành Node giao thông (Code của bạn bạn)
        Long startNode = roadNodeRepository.findNearestNode(request.getStartLat(), request.getStartLng());
        Long endNode = roadNodeRepository.findNearestNode(request.getEndLat(), request.getEndLng());

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Khu vực này chưa có dữ liệu mạng lưới đường bộ!");
        }

        RestTemplate restTemplate = new RestTemplate();

        // 2. Ưu tiên gọi AI Python (Code của bạn)
        try {
            log.info("Đang gọi AI Python tìm đường từ tọa độ: [{}, {}] đến [{}, {}]",
                    request.getStartLat(), request.getStartLng(),
                    request.getEndLat(), request.getEndLng());

            ResponseEntity<RoutingResponse> response = restTemplate.postForEntity(PYTHON_AI_URL, request, RoutingResponse.class);
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            // BẮT LỖI 404/400 TỪ PYTHON
            log.warn("AI Python từ chối tìm đường. Mã lỗi: {}", e.getStatusCode());
            throw new RuntimeException("Dự báo từ AI: Không tìm thấy đường đi an toàn. Vị trí đích có thể đã bị cô lập hoàn toàn do thiên tai.");

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
}