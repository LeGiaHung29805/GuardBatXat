package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.commander.CommanderLandslideProjection;
import java.util.List;

public interface CommanderDashboardService {
    List<CommanderFloodProjection> getCommanderFloodHeatmap(Double waterLevel);
    List<CommanderLandslideProjection> getCommanderLandslideHeatmap();
    List<String> getAvailableScenarios();
    java.util.Map<String, Object> getDashboardStats(Double level);
}
