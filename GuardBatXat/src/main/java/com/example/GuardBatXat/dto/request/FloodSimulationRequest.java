package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FloodSimulationRequest {
    @NotNull(message = "Mức nước lũ không được để trống")
    @Positive(message = "Mức nước phải là số dương")
    private Double waterLevel; // Ví dụ: 83.5
}