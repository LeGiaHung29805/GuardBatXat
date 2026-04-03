package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.FloodStatisticDto;

import java.util.List;

public interface FloodSimulationService {
    List<FloodSimulationResponse> runSimulation(Double waterLevel);
    List<FloodStatisticDto> getSimulationStatistics(String simulationId);
}