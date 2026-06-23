package com.example.GuardBatXat.dto.response.admin;
import com.example.GuardBatXat.entity.AhpWeight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeightResponse {
    private String strategyName;
    private Double wDistance;
    private Double wFlood;
    private Double wLandslide;
    private Double wCapacity;
    private Double wBridge;
    private Double wReport;
}
