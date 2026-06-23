package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.Building;

import com.example.GuardBatXat.repository.CommanderAnalysisRepository;
import com.example.GuardBatXat.service.CommanderAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommanderAnalysisServiceImpl implements CommanderAnalysisService {

    private final CommanderAnalysisRepository analysisRepository;

    @Override
    public List<Map<String, Object>> getDamageByType(Double level) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        Integer floodedBuildings = analysisRepository.countFloodedBuildings(level);
        Integer floodedRoads = analysisRepository.countFloodedRoads(level);
        Integer landslideRisk = analysisRepository.countLandslideRiskBuildings();
        Integer damagedInfra = analysisRepository.countDamagedInfrastructure(level);

        data.add(Map.of("loai_thiet_hai", "Nhà ngập", "so_luong", floodedBuildings));
        data.add(Map.of("loai_thiet_hai", "Đường chặn", "so_luong", floodedRoads));
        data.add(Map.of("loai_thiet_hai", "Sạt lở", "so_luong", landslideRisk));
        data.add(Map.of("loai_thiet_hai", "Hạ tầng hư hỏng", "so_luong", damagedInfra));
        
        return data;
    }

    @Override
    public List<Map<String, Object>> getSeverityChart(Double level) {
        return analysisRepository.getSeverityChart(level);
    }

    @Override
    public List<Map<String, Object>> getDamageTrend() {
        return analysisRepository.getDamageTrend();
    }

    @Override
    public List<Map<String, Object>> getTopAreas(Double level) {
        return analysisRepository.getTopAreas(level);
    }

    @Override
    public List<Map<String, Object>> getCommuneRanking(Double level) {
        return analysisRepository.getCommuneRanking(level);
    }

    @Override
    public List<Map<String, Object>> getWaterLevelForecast() {
        return analysisRepository.getWaterLevelForecast();
    }
}
