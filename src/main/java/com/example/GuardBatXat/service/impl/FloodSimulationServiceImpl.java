package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.entity.Building;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.commander.FloodStatisticDto;
import com.example.GuardBatXat.entity.FloodSimulation;
import com.example.GuardBatXat.repository.FloodSimulationRepository;
import com.example.GuardBatXat.service.FloodSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.GuardBatXat.websocket.NotificationSender;

@Service
@RequiredArgsConstructor
public class FloodSimulationServiceImpl implements FloodSimulationService {

    private final FloodSimulationRepository simulationRepository;
    private final NotificationSender notificationSender;

    @Override
    @Transactional
    public List<FloodSimulationResponse> runSimulation(Double waterLevel) {
        // 1. Sinh một ID kịch bản ngẫu nhiên cho lần chạy này
        String simId = UUID.randomUUID().toString();

        // 2. Kích hoạt hàm Native SQL dưới Database để tính toán và lưu kết quả
        simulationRepository.executeFloodSimulationNative(simId, waterLevel);

        // 3. Kéo kết quả vừa tính xong lên
        List<FloodSimulation> results = simulationRepository.findBySimulationId(UUID.fromString(simId));

        // 4. Map dữ liệu sang DTO, đồng thời chiết xuất tọa độ (X, Y) từ MultiPolygon
        List<FloodSimulationResponse> responseList = results.stream().map(sim -> FloodSimulationResponse.builder()
                .buildingId(sim.getBuilding().getId())
                .depth(sim.getDepthImpact())
                .status(sim.getRiskStatus())
                .lng(sim.getBuilding().getGeom().getCentroid().getX())
                .lat(sim.getBuilding().getGeom().getCentroid().getY())
                .build()
        ).collect(Collectors.toList());

        // 5. Bắn WebSocket thông báo có kết quả mô phỏng mới
        try {
            notificationSender.sendSystemNotification("/topic/simulation-results", responseList);
        } catch (Exception e) {
            // Log lỗi nhưng không làm fail API
            e.printStackTrace();
        }

        return responseList;
    }

    @Override
    public List<FloodStatisticDto> getSimulationStatistics(String simulationId) {
        return simulationRepository.getStatisticsBySimulationId(UUID.fromString(simulationId));
    }
}