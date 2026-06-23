package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.commander.NotificationSendRequest;
import com.example.GuardBatXat.dto.response.commander.NotificationResponse;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void triggerEvacuation(String level, Double radius);
    void sendNotification(NotificationSendRequest request);
    List<NotificationResponse> getNotificationHistory();
    Map<String, Double> getEvacuationCenter(String level);
}
