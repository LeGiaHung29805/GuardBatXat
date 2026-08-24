package com.example.GuardBatXat.dto.response.admin;
import com.example.GuardBatXat.entity.AhpWeight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeightResponse {
    private String strategyName;
    private BigDecimal wDistance;
    private BigDecimal wFlood;
    private BigDecimal wLandslide;
    private BigDecimal wCapacity;
    private BigDecimal wBridge;
    private BigDecimal wReport;
}
