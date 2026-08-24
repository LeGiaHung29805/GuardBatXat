package com.example.GuardBatXat.dto.request.admin;
import com.example.GuardBatXat.entity.AhpWeight;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeightRequest {

    @NotNull(message = "wDistance không được để trống")
    @JsonProperty("wDistance")
    private BigDecimal wDistance;

    @NotNull(message = "wFlood không được để trống")
    @JsonProperty("wFlood")
    private BigDecimal wFlood;

    @NotNull(message = "wLandslide không được để trống")
    @JsonProperty("wLandslide")
    private BigDecimal wLandslide;

    @NotNull(message = "wCapacity không được để trống")
    @JsonProperty("wCapacity")
    private BigDecimal wCapacity;

    @NotNull(message = "wBridge không được để trống")
    @JsonProperty("wBridge")
    private BigDecimal wBridge;

    @NotNull(message = "wReport không được để trống")
    @JsonProperty("wReport")
    private BigDecimal wReport;
}
