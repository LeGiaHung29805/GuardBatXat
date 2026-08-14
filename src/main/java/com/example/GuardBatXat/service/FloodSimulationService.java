package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.FloodSimulation;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationRunResponse;
import com.example.GuardBatXat.dto.response.commander.FloodStatisticDto;

import java.util.List;

public interface FloodSimulationService {
    FloodSimulationRunResponse runSimulation(Double waterLevel);
    List<FloodStatisticDto> getSimulationStatistics(String simulationId);
}
