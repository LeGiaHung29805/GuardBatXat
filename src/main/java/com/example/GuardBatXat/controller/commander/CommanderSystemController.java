package com.example.GuardBatXat.controller.commander;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.commander.NotificationSendRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.commander.NotificationResponse;
import com.example.GuardBatXat.service.CommanderDashboardService;
import com.example.GuardBatXat.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commander")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommanderSystemController {

    private final CommanderDashboardService commanderDashboardService;
    private final NotificationService notificationService;

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
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Lấy thống kê thành công")
                .data(commanderDashboardService.getDashboardStats(level))
                .build());
    }

    @PostMapping("/evacuation/activate")
    public ResponseEntity<ApiResponse<Map<String, String>>> activateEvacuation(
            @RequestParam("level") String level,
            @RequestParam(value = "radius", defaultValue = "1000") Double radius) {

        notificationService.triggerEvacuation(level, radius);

        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Đã kích hoạt lệnh sơ tán khẩn cấp!")
                .build());
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendNotification(@RequestBody NotificationSendRequest payload) {
        notificationService.sendNotification(payload);

        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Đã gửi cảnh báo và lưu vào lịch sử!")
                .build());
    }

    @GetMapping("/notifications/history")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationHistory() {
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .message("Lấy lịch sử thông báo thành công")
                .data(notificationService.getNotificationHistory())
                .build());
    }
}
