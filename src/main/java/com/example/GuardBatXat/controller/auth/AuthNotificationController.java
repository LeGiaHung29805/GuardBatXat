package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.commander.NotificationResponse;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthNotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationHistory() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = null;
        if (identifier != null && !identifier.isEmpty() && !"anonymousUser".equals(identifier)) {
            currentUser = userRepository.findByIdentifier(identifier).orElse(null);
        }

        List<NotificationResponse> data = notificationService.getNotificationHistoryForCitizen(currentUser);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .message("Lấy lịch sử thông báo thành công")
                .data(data)
                .build());
    }
}
