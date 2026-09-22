package com.example.GuardBatXat.dto.response.commander;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloodSimulationRunResponse {
    private String simulationId;
    private List<FloodSimulationResponse> results;
}
