package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.websocket.NotificationSender;
import com.example.GuardBatXat.entity.RoadNode;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.rescue.FindShelterRequest;
import com.example.GuardBatXat.dto.request.rescue.RoutingRequest;
import com.example.GuardBatXat.dto.response.rescue.RoutingCompareResponse;
import com.example.GuardBatXat.dto.response.rescue.RoutingResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    @org.springframework.beans.factory.annotation.Value("${batxat.ai.service.base-url:http://localhost:5000}")
    private String aiServiceBaseUrl;

    private final RoadNodeRepository roadNodeRepository;
    private final com.example.GuardBatXat.websocket.NotificationSender notificationSender;
    private final RestTemplate restTemplate;

    private String getPythonAiUrl() {
        return aiServiceBaseUrl + "/api/v1/ai/safe-routing";
    }

    private String getPythonShelterUrl() {
        return aiServiceBaseUrl + "/api/v1/ai/find-safe-shelter";
    }

    private String getPythonAdminCompareUrl() {
        return aiServiceBaseUrl + "/api/v1/ai/admin-routing";
    }

    @Override
    public Object getSafeRouteFromAI(RoutingRequest request) {
        try {
            log.info("Đang gọi AI Python tìm đường tại: {}", getPythonAiUrl());
            try {
                notificationSender.sendSystemNotification("/topic/task-progress", "Đang khởi tạo thuật toán AI tìm đường đi an toàn...");
            } catch (Exception e) {}

            // NHẬN VỀ Map.class ĐỂ TRÍCH XUẤU DỮ LIỆU LINH HOẠT
            ResponseEntity<Map> response = restTemplate.postForEntity(getPythonAiUrl(), request, Map.class);
            Map<String, Object> body = response.getBody();

            try {
                notificationSender.sendSystemNotification("/topic/task-progress", "AI xử lý thành công, đang trả về kết quả định tuyến.");
            } catch (Exception e) {}

            if (body != null && "success".equals(body.get("status"))) {
                // 1. CHUYỂN ĐỔI TOẠ ĐỘ
                List<List<Double>> rawCoords = (List<List<Double>>) body.get("route_coordinates");
                List<double[]> pathPoints = new ArrayList<>();

                if (rawCoords != null) {
                    for (List<Double> point : rawCoords) {
                        if (point != null && point.size() >= 2) {
                            pathPoints.add(new double[]{point.get(0), point.get(1)});
                        }
                    }
                }

                // 2. LẤY CHI PHÍ
                Double cost = Double.valueOf(body.get("total_mcdm_cost").toString());

                // 3. KHAI BÁO STRATEGY NAME
                String strategyName = request.getStrategyName();
                if (body.get("strategy") != null) {
                    strategyName = body.get("strategy").toString();
                }

                return RoutingResponse.builder()
                        .strategyName(strategyName)
                        .totalDistance(cost)
                        .pathPoints(pathPoints)
                        .build();
            }
            throw new RuntimeException("AI không tìm được đường");
        } catch (Exception e) {
            log.error("Lỗi mạng khi kết nối với module AI Python: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI phân tích lộ trình đang bảo trì hoặc mất kết nối mạng!");
        }
    }

    @Override
    public Object findSafeShelterFromAI(FindShelterRequest request) {
        try {
            log.info("Đang gọi AI tìm điểm sơ tán cho tọa độ: [{}, {}] - Chiến lược: {} tại: {}",
                    request.getCurrentLat(), request.getCurrentLng(),
                    request.getStrategy() != null ? request.getStrategy() : "safety",
                    getPythonShelterUrl());

            ResponseEntity<Object> response = restTemplate.postForEntity(getPythonShelterUrl(), request, Object.class);
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            log.warn("AI báo không tìm thấy điểm sơ tán hợp lệ. Mã lỗi: {}", e.getStatusCode());
            throw new RuntimeException("Cảnh báo AI: Không có điểm sơ tán nào khả dụng hoặc mọi ngả đường đều đã bị cô lập do thiên tai!");

        } catch (Exception e) {
            log.error("Lỗi kết nối AI: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI tìm điểm sơ tán đang bảo trì hoặc mất kết nối mạng!");
        }
    }

    @Override
    public RoutingCompareResponse findAdminCompareRoute(RoutingRequest request) {
        Long startNode = roadNodeRepository.findNearestNode(request.getStartLat(), request.getStartLng());
        Long endNode = roadNodeRepository.findNearestNode(request.getEndLat(), request.getEndLng());

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Khu vực chưa có dữ liệu mạng lưới giao thông!");
        }

        try {
            log.info("Admin đang kiểm chứng 3 lộ trình từ {} đến {} tại: {}", request.getStartLat(), request.getEndLat(), getPythonAdminCompareUrl());
            try {
                notificationSender.sendSystemNotification("/topic/task-progress", "Hệ thống AI đang phân tích và đối chiếu 3 chiến lược định tuyến. Quá trình này có thể mất vài giây...");
            } catch (Exception e) {}

            ResponseEntity<Map> response = restTemplate.postForEntity(getPythonAdminCompareUrl(), request, Map.class);
            Map<String, Object> body = response.getBody();

            try {
                notificationSender.sendSystemNotification("/topic/task-progress", "Đã hoàn tất phân tích đối chiếu 3 lộ trình.");
            } catch (Exception e) {}

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
            throw new RuntimeException("Dữ liệu thực tế báo cáo khu vực này đang bị cô lập, không có tuyến đường an toàn.");
        }
    }

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

    @Override
    public RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request) {
        request.setStrategyName(strategyName);
        Object response = getSafeRouteFromAI(request);
        if (response instanceof RoutingResponse) {
            return (RoutingResponse) response;
        }
        throw new RuntimeException("Lỗi định tuyến tối ưu");
    }
} // Kết thúc class
