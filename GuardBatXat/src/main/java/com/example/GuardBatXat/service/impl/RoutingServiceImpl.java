package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RoutingServiceImpl implements RoutingService {

    // Đường dẫn gốc gọi sang server Python (chạy ở port 5000)
    private final String PYTHON_AI_URL = "http://localhost:5000/api/v1/ai/safe-routing";

    @Override
    public Object getSafeRouteFromAI(RoutingRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            log.info("Đang gọi AI Python tìm đường từ tọa độ: [{}, {}] đến [{}, {}]",
                    request.getStartLat(), request.getStartLng(),
                    request.getEndLat(), request.getEndLng());

            // Spring Boot "đóng gói" Request và bắn sang Python, sau đó hứng kết quả
            ResponseEntity<Object> response = restTemplate.postForEntity(PYTHON_AI_URL, request, Object.class);

            log.info("Nhận kết quả tìm đường từ AI thành công!");
            return response.getBody(); // Trả nguyên cục JSON của Python về

        } catch (Exception e) {
            log.error("Lỗi khi kết nối với module AI Python: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI tìm đường đang bảo trì hoặc mất kết nối!");
        }
    }
}