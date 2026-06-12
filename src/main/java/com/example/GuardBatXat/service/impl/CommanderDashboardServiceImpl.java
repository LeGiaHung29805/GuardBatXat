package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.repository.FloodSimulationRepository;
import com.example.GuardBatXat.repository.CommanderAnalysisRepository;
import com.example.GuardBatXat.entity.FloodSimulation;

import com.example.GuardBatXat.dto.response.commander.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.commander.CommanderLandslideProjection;
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
    private final com.example.GuardBatXat.repository.CommanderAnalysisRepository analysisRepository;

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

    @Override
    public java.util.Map<String, Object> getDashboardStats(Double level) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 1. Lấy dữ liệu ngập lụt TỰ ĐỘNG KHỚP với Heatmap
        List<CommanderFloodProjection> floodData = getCommanderFloodHeatmap(level);

        int nhaBiNgap = floodData.size();
        int nguoiCanSoTan = floodData.stream().mapToInt(f -> f.getSo_nguoi() != null ? f.getSo_nguoi() : 0).sum();
        int dienTichNgap = (int) floodData.stream().mapToDouble(f -> f.getDien_tich() != null ? f.getDien_tich() : 0.0).sum();

        // 2. Đồng bộ "Đường bị chặn" với phần Phân tích (Gộp cả Ngập lụt và Sạt lở)
        int duongBiChan = analysisRepository.countFloodedRoads(level);

        stats.put("nha_bi_ngap", nhaBiNgap);
        stats.put("dien_tich_ngap_m2", dienTichNgap);
        stats.put("nguoi_can_so_tan", nguoiCanSoTan);
        stats.put("duong_bi_chan", duongBiChan);

        return stats;
    }
}
