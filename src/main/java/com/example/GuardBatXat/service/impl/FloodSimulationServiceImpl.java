package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.response.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.FloodStatisticDto;
import com.example.GuardBatXat.entity.FloodSimulation;
import com.example.GuardBatXat.repository.FloodSimulationRepository;
import com.example.GuardBatXat.service.FloodSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FloodSimulationServiceImpl implements FloodSimulationService {

    private final FloodSimulationRepository simulationRepository;

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
        return results.stream().map(sim -> FloodSimulationResponse.builder()
                .buildingId(sim.getBuilding().getId())
                .depth(sim.getDepthImpact())
                .status(sim.getRiskStatus())
                .lng(sim.getBuilding().getGeom().getCentroid().getX())
                .lat(sim.getBuilding().getGeom().getCentroid().getY())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<FloodStatisticDto> getSimulationStatistics(String simulationId) {
        return simulationRepository.getStatisticsBySimulationId(UUID.fromString(simulationId));
    }
}