package com.example.GuardBatXat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 1. CHỈ ĐỂ LẠI 1 CLASS DUY NHẤT VÀ GẮN @RestController VÀO ĐÂY
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHealthCheckController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Dùng để test kết nối PostGIS

    @Autowired
    private RestTemplate restTemplate;

    @Value("${batxat.ai.service.base-url:http://localhost:5000}")
    private String aiServiceBaseUrl;

    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        Map<String, String> data = new HashMap<>();

        // 1. Server chắc chắn "ok" nếu nó lọt được vào hàm này
        data.put("server", "ok");

        // 2. Check PostGIS (query thử một câu lệnh đơn giản)
        try {
            jdbcTemplate.execute("SELECT 1");
            data.put("database", "ok");
        } catch (Exception e) {
            data.put("database", "error");
        }

        // 3. Check AI Engine (Ping thử sang Flask/FastAPI của Python qua RestTemplate)
        try {
            Map<?, ?> aiHealth = restTemplate.getForObject(
                    aiServiceBaseUrl + "/api/v1/health",
                    Map.class
            );
            data.put("ai", aiHealth != null && "ok".equals(aiHealth.get("status")) ? "ok" : "error");
        } catch (Exception exception) {
            data.put("ai", "error");
        }

        // Gói lại thành format ApiResponse chung của hệ thống
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Health check completed");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
