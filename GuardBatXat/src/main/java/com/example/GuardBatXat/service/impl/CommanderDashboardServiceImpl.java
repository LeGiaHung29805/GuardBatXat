package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.response.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.CommanderLandslideProjection;
import com.example.GuardBatXat.repository.CommanderMapRepository;
import com.example.GuardBatXat.service.CommanderDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommanderDashboardServiceImpl implements CommanderDashboardService {

    private final CommanderMapRepository commanderMapRepository;
    private final com.example.GuardBatXat.repository.FloodSimulationRepository floodSimulationRepository;

    @Override
    public List<CommanderFloodProjection> getCommanderFloodHeatmap(Double waterLevel) {
        log.info("Lấy dữ liệu Bản đồ ngập lụt Commander cho kịch bản: {}m", waterLevel);
        return commanderMapRepository.getCommanderFloodHeatmap(waterLevel);
    }

    @Override
    public List<CommanderLandslideProjection> getCommanderLandslideHeatmap() {
        log.info("Lấy dữ liệu Bản đồ sạt lở Commander cho ngày hiện tại");
        return commanderMapRepository.getCommanderLandslideHeatmap();
    }

    @Override
    public java.util.List<String> getAvailableScenarios() {
        java.util.List<String> scenarios = new java.util.ArrayList<>();
        scenarios.add("80m");
        scenarios.add("82m");
        scenarios.add("83.5m");

        // Lấy danh sách từ DB và nối thêm (nếu chưa có)
        try {
            java.util.List<Double> dbLevels = floodSimulationRepository.findAllSimulatedLevels();
            for (Double level : dbLevels) {
                String strLevel = level + "m";
                // Lọc bỏ ".0m" thành "m" cho đẹp nếu cần, ở đây để nguyên dạng <number>m
                if (level == Math.floor(level)) {
                    strLevel = level.intValue() + "m";
                }
                if (!scenarios.contains(strLevel)) {
                    scenarios.add(strLevel);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách mô phỏng: {}", e.getMessage());
        }

        return scenarios;
    }
}
