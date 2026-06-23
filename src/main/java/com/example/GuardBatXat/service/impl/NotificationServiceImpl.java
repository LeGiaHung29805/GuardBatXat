package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.Building;

import com.example.GuardBatXat.dto.request.commander.NotificationSendRequest;
import com.example.GuardBatXat.dto.response.commander.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.commander.NotificationResponse;
import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.repository.NotificationRepository;
import com.example.GuardBatXat.service.CommanderDashboardService;
import com.example.GuardBatXat.service.NotificationService;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final CommanderDashboardService commanderDashboardService;

    @Override
    @Transactional
    public void triggerEvacuation(String level, Double radius) {
        // 1. Lưu thông báo vào Database
        Notification notification = new Notification();
        notification.setTitle("Lệnh Sơ Tán Khẩn Cấp");
        notification.setContent("Yêu cầu toàn bộ người dân trong vùng ngập lụt " + level + " sơ tán ngay lập tức!");
        notification.setAlertLevel("Kịch bản " + level);
        notificationRepository.save(notification);

        // 2. Tính toán tọa độ tâm của vùng ngập (dùng chung 1 hàm để client preview khớp 100%)
        Map<String, Double> center = getEvacuationCenter(level);

        // 3. Gửi thông báo Real-time qua WebSocket
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("type", "MANUAL_ALERT");
        wsPayload.put("title", notification.getTitle());
        wsPayload.put("content", notification.getContent());
        wsPayload.put("level", level);
        wsPayload.put("targetArea", "Toàn Vùng");
        wsPayload.put("centerLat", center.get("centerLat"));
        wsPayload.put("centerLng", center.get("centerLng"));
        wsPayload.put("radius", radius);

        notificationSender.sendSystemNotification("/topic/alerts", wsPayload);
    }

    @Override
    public Map<String, Double> getEvacuationCenter(String level) {
        // Tâm mặc định của Bát Xát (khi không có điểm ngập nào)
        double centerLat = 22.6133;
        double centerLng = 103.8647;

        Double numLevel = Double.valueOf(level);
        List<CommanderFloodProjection> floodedBuildings = commanderDashboardService.getCommanderFloodHeatmap(numLevel);

        if (floodedBuildings != null && !floodedBuildings.isEmpty()) {
            double sumLat = 0;
            double sumLng = 0;
            for (CommanderFloodProjection f : floodedBuildings) {
                sumLat += f.getLat() != null ? f.getLat() : centerLat;
                sumLng += f.getLng() != null ? f.getLng() : centerLng;
            }
            centerLat = sumLat / floodedBuildings.size();
            centerLng = sumLng / floodedBuildings.size();
        }

        Map<String, Double> center = new HashMap<>();
        center.put("centerLat", centerLat);
        center.put("centerLng", centerLng);
        return center;
    }

    @Override
    @Transactional
    public void sendNotification(NotificationSendRequest request) {
        // 1. Lưu thông báo vào Database
        Notification notification = new Notification();
        notification.setTitle(request.getTitle() != null ? request.getTitle() : "Cảnh báo khẩn cấp");
        notification.setContent(request.getContent());
        notification.setAlertLevel(request.getTargetArea() != null ? request.getTargetArea() : request.getLevel());
        notificationRepository.save(notification);

        // 2. Gửi thông báo Real-time qua WebSocket
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("type", "MANUAL_ALERT");
        wsPayload.put("title", notification.getTitle());
        wsPayload.put("content", notification.getContent());
        wsPayload.put("level", request.getLevel());
        wsPayload.put("targetArea", request.getTargetArea());

        notificationSender.sendSystemNotification("/topic/alerts", wsPayload);
    }

    @Override
    public List<NotificationResponse> getNotificationHistory() {
        List<Notification> realNotifications = notificationRepository.findTop20ByOrderByCreatedAtDesc();

        return realNotifications.stream().map(n -> {
            String time = n.getCreatedAt() != null 
                ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            return NotificationResponse.builder()
                    .notifyId(n.getNotifyId() != null ? n.getNotifyId().longValue() : null)
                    .targetArea(n.getAlertLevel() != null ? n.getAlertLevel() : "Huyện Bát Xát")
                    .title(n.getTitle() != null ? n.getTitle() : "Thông báo")
                    .content(n.getContent())
                    .time(time)
                    .build();
        }).collect(Collectors.toList());
    }
}
