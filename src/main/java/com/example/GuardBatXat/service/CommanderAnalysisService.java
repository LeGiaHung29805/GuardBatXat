package com.example.GuardBatXat.service;

import java.util.List;
import java.util.Map;

public interface CommanderAnalysisService {
    List<Map<String, Object>> getDamageByType(Double level);
    List<Map<String, Object>> getSeverityChart(Double level);
    List<Map<String, Object>> getDamageTrend();
    List<Map<String, Object>> getTopAreas(Double level);
    List<Map<String, Object>> getCommuneRanking(Double level);
    List<Map<String, Object>> getWaterLevelForecast();
}
