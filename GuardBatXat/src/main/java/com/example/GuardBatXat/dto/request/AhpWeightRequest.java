package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeightRequest {
    @NotNull private Double wDistance;
    @NotNull private Double wFlood;
    @NotNull private Double wLandslide;
    @NotNull private Double wCapacity;
    @NotNull private Double wBridge;
    @NotNull private Double wReport;
}