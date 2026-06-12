package com.example.GuardBatXat.dto.response.rescue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosResponse {
    private Integer id;
    private String senderName;
    private String senderPhone;
    private String message;
    private String status;
    private Double gpsLat;
    private Double gpsLng;
    private Integer totalPeople;
    private LocalDateTime createdAt;
    private String assignedTo; // username of the rescue team
}
