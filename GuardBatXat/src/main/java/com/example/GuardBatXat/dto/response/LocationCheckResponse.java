package com.example.GuardBatXat.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationCheckResponse {
    private boolean isSafe;
    private String alertLevel; // SAFE, WARNING, DANGER
    private String message;

    // Chi tiết rủi ro
    private String floodRiskStatus;
    private String landslideRiskStatus;
    private Double floodDepth;     // Độ sâu ngập dự kiến (m)
    private Double aiLandslideProb;// Xác suất sạt lở AI (%)

    // Thông tin tòa nhà người dùng đang đứng
    private String buildingType;
    private Double distanceToWater;
    private Double currentElevation; // Cao độ hiện tại
}