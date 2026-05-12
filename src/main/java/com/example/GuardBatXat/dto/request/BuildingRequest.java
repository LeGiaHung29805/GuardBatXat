package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingRequest {
    private Double areaInMeters;
    private Double elevationZ;
    private String buildingType;
    private Integer maxCapacity;
    private Integer estimatedPop;

    @NotBlank(message = "Tọa độ không gian (WKT) không được để trống")
    private String geomWkt; // Ví dụ: "MULTIPOLYGON(((103.8 22.5, 103.9 22.6, 103.8 22.5)))"
}