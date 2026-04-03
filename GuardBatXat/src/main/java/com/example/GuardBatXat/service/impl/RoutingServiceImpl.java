package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException; // Nhớ import cái này
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RoutingServiceImpl implements RoutingService {

    private final String PYTHON_AI_URL = "http://localhost:5000/api/v1/ai/safe-routing";
    private final String PYTHON_SHELTER_URL = "http://localhost:5000/api/v1/ai/find-safe-shelter";
    @Override
    public Object getSafeRouteFromAI(RoutingRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            log.info("Đang gọi AI Python tìm đường từ tọa độ: [{}, {}] đến [{}, {}]",
                    request.getStartLat(), request.getStartLng(),
                    request.getEndLat(), request.getEndLng());

            ResponseEntity<Object> response = restTemplate.postForEntity(PYTHON_AI_URL, request, Object.class);
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            // BẮT LỖI 404/400 TỪ PYTHON (Tức là gọi được nhưng AI bảo không có đường)
            log.warn("AI Python từ chối tìm đường. Mã lỗi: {}", e.getStatusCode());
            throw new RuntimeException("Dự báo từ AI: Không tìm thấy đường đi an toàn. Vị trí đích có thể đã bị cô lập hoàn toàn do thiên tai.");

        } catch (Exception e) {
            // BẮT LỖI MẤT KẾT NỐI (Tức là Server Python đang tắt)
            log.error("Lỗi mạng khi kết nối với module AI Python: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI tìm đường đang bảo trì hoặc mất kết nối mạng!");
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