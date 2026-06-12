package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.CommanderLandslideProjection;
import java.util.List;

public interface CommanderDashboardService {
    List<CommanderFloodProjection> getCommanderFloodHeatmap(Double waterLevel);
    List<CommanderLandslideProjection> getCommanderLandslideHeatmap();
    List<String> getAvailableScenarios();
}
