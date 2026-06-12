package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.CommanderLandslideProjection;
import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.repository.NotificationRepository;
import com.example.GuardBatXat.service.CommanderDashboardService;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commander")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommanderSystemController {

    private final CommanderDashboardService commanderDashboardService;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final com.example.GuardBatXat.repository.CommanderAnalysisRepository analysisRepository;

    @GetMapping("/statistics/scenarios")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableScenarios() {
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .code(200)
                .message("Lấy danh sách kịch bản mô phỏng thành công")
                .data(commanderDashboardService.getAvailableScenarios())
                .build());
    }

    @GetMapping("/statistics/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(@RequestParam(value = "level", defaultValue = "80") Double level) {
        Map<String, Object> stats = new HashMap<>();

        // 1. Lấy dữ liệu ngập lụt TỰ ĐỘNG KHỚP với Heatmap
        List<CommanderFloodProjection> floodData = commanderDashboardService.getCommanderFloodHeatmap(level);

        int nhaBiNgap = floodData.size();
        int nguoiCanSoTan = floodData.stream().mapToInt(f -> f.getSo_nguoi() != null ? f.getSo_nguoi() : 0).sum();
        int dienTichNgap = (int) floodData.stream().mapToDouble(f -> f.getDien_tich() != null ? f.getDien_tich() : 0.0).sum();

        // 2. Đồng bộ "Đường bị chặn" với phần Phân tích (Gộp cả Ngập lụt và Sạt lở)
        int duongBiChan = analysisRepository.countFloodedRoads(level);

        stats.put("nha_bi_ngap", nhaBiNgap);
        stats.put("dien_tich_ngap_m2", dienTichNgap);
        stats.put("nguoi_can_so_tan", nguoiCanSoTan);
        stats.put("duong_bi_chan", duongBiChan);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Lấy thống kê thành công")
                .data(stats)
                .build());
    }

    @PostMapping("/evacuation/activate")
    public ResponseEntity<ApiResponse<Map<String, String>>> activateEvacuation(
            @RequestParam("level") String level,
            @RequestParam(value = "radius", defaultValue = "1000") Double radius) {

        // 1. Lưu thông báo vào Database
        Notification notification = new Notification();
        notification.setTitle("Lệnh Sơ Tán Khẩn Cấp");
        notification.setContent("Yêu cầu toàn bộ người dân trong vùng ngập lụt " + level + " sơ tán ngay lập tức!");
        notification.setAlertLevel("Kịch bản " + level);
        notificationRepository.save(notification);

        // 2. Tính toán tọa độ tâm của vùng ngập
        Double numLevel = Double.valueOf(level);
        List<CommanderFloodProjection> floodedBuildings = commanderDashboardService.getCommanderFloodHeatmap(numLevel);

        double centerLat = 22.6133; // Tâm mặc định của Bát Xát
        double centerLng = 103.8647;

        if (!floodedBuildings.isEmpty()) {
            double sumLat = 0;
            double sumLng = 0;
            for (CommanderFloodProjection f : floodedBuildings) {
                sumLat += f.getLat() != null ? f.getLat() : centerLat;
                sumLng += f.getLng() != null ? f.getLng() : centerLng;
            }
            centerLat = sumLat / floodedBuildings.size();
            centerLng = sumLng / floodedBuildings.size();
        }

        // 3. Gửi thông báo Real-time qua WebSocket cho các Client (Frontend)
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("type", "MANUAL_ALERT");
        wsPayload.put("title", notification.getTitle());
        wsPayload.put("content", notification.getContent());
        wsPayload.put("level", level);
        wsPayload.put("targetArea", "Toàn Vùng");
        wsPayload.put("centerLat", centerLat);
        wsPayload.put("centerLng", centerLng);
        wsPayload.put("radius", radius);

        notificationSender.sendSystemNotification("/topic/alerts", wsPayload);

        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Đã kích hoạt lệnh sơ tán khẩn cấp!")
                .build());
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendNotification(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String content = payload.get("content");
        String level = payload.get("level");
        String targetArea = payload.get("targetArea");

        // 1. Lưu thông báo vào Database
        Notification notification = new Notification();
        notification.setTitle(title != null ? title : "Cảnh báo khẩn cấp");
        notification.setContent(content);
        notification.setAlertLevel(targetArea != null ? targetArea : level);
        // createdAt is set by DB default, but we can set it here if needed, or leave it for DB
        notificationRepository.save(notification);

        // 2. Gửi thông báo Real-time qua WebSocket cho các Client (Frontend)
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("type", "MANUAL_ALERT");
        wsPayload.put("title", notification.getTitle());
        wsPayload.put("content", notification.getContent());
        wsPayload.put("level", level);
        wsPayload.put("targetArea", targetArea);

        notificationSender.sendSystemNotification("/topic/alerts", wsPayload);

        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Đã gửi cảnh báo và lưu vào lịch sử!")
                .build());
    }

    @GetMapping("/notifications/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotificationHistory() {
        // Lấy dữ liệu cảnh báo thực tế từ Database
        List<Notification> realNotifications = notificationRepository.findTop20ByOrderByCreatedAtDesc();

        List<Map<String, Object>> history = realNotifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("notify_id", n.getNotifyId());
            map.put("target_area", n.getAlertLevel() != null ? n.getAlertLevel() : "Huyện Bát Xát");
            map.put("title", n.getTitle() != null ? n.getTitle() : "Thông báo");
            map.put("content", n.getContent());

            // Format thời gian
            String thoiGian = "";
            if (n.getCreatedAt() != null) {
                thoiGian = n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            map.put("thoi_gian", thoiGian);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Lấy lịch sử thông báo thành công")
                .data(history)
                .build());
    }
}
