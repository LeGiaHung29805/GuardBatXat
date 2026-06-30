package com.example.GuardBatXat.dto.response.commander;
import com.example.GuardBatXat.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notifyId;
    private String targetArea;
    private String title;
    private String content;
    private String time;
    private Boolean isPersonal;
}
