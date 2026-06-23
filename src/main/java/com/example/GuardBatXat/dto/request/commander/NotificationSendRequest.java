package com.example.GuardBatXat.dto.request.commander;
import com.example.GuardBatXat.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequest {
    private String title;
    private String content;
    private String level;
    private String targetArea;
}
