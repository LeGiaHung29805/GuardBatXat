package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRequest {

    @NotNull(message = "Vĩ độ điểm bắt đầu (startLat) không được để trống")
    private Double startLat;

    @NotNull(message = "Kinh độ điểm bắt đầu (startLng) không được để trống")
    private Double startLng;

    @NotNull(message = "Vĩ độ điểm kết thúc (endLat) không được để trống")
    private Double endLat;

    @NotNull(message = "Kinh độ điểm kết thúc (endLng) không được để trống")
    private Double endLng;
    private String strategyName;
}