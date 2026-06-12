package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.commander.NotificationSendRequest;
import com.example.GuardBatXat.dto.response.commander.NotificationResponse;

import java.util.List;

public interface NotificationService {
    void triggerEvacuation(String level, Double radius);
    void sendNotification(NotificationSendRequest request);
    List<NotificationResponse> getNotificationHistory();
}
