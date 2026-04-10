package com.example.GuardBatXat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeightRequest {

    @NotNull(message = "wDistance không được để trống")
    @JsonProperty("wDistance")
    private Double wDistance;

    @NotNull(message = "wFlood không được để trống")
    @JsonProperty("wFlood")
    private Double wFlood;

    @NotNull(message = "wLandslide không được để trống")
    @JsonProperty("wLandslide")
    private Double wLandslide;

    @NotNull(message = "wCapacity không được để trống")
    @JsonProperty("wCapacity")
    private Double wCapacity;

    @NotNull(message = "wBridge không được để trống")
    @JsonProperty("wBridge")
    private Double wBridge;

    @NotNull(message = "wReport không được để trống")
    @JsonProperty("wReport")
    private Double wReport;
}