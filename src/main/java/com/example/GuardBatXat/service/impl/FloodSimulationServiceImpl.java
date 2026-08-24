package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationRunResponse;
import com.example.GuardBatXat.dto.response.commander.FloodStatisticDto;
import com.example.GuardBatXat.repository.FloodSimulationRepository;
import com.example.GuardBatXat.repository.projection.FloodSimulationMapPoint;
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
    public FloodSimulationRunResponse runSimulation(Double waterLevel) {
        // 1. Sinh một ID kịch bản ngẫu nhiên cho lần chạy này
        String simId = UUID.randomUUID().toString();

        // 2. Kích hoạt hàm Native SQL dưới Database để tính toán và lưu kết quả
        simulationRepository.executeFloodSimulationNative(simId, waterLevel);

        // 3. DB trả thẳng dữ liệu bản đồ tối thiểu; không nạp entity Building/geometry đầy đủ.
        List<FloodSimulationMapPoint> results = simulationRepository.findMapPointsBySimulationId(UUID.fromString(simId));

        // 4. Map dữ liệu đã được PostGIS tính tọa độ điểm đại diện nằm trong công trình.
        List<FloodSimulationResponse> responseList = results.stream().map(point -> FloodSimulationResponse.builder()
                .buildingId(point.getBuildingId())
                .depth(point.getDepth())
                .status(point.getStatus())
                .lng(point.getLng())
                .lat(point.getLat())
                .build()
        ).collect(Collectors.toList());

        // 5. Bắn WebSocket thông báo có kết quả mô phỏng mới
        try {
            notificationSender.sendSystemNotification("/topic/simulation-results", responseList);
        } catch (Exception e) {
            // Log lỗi nhưng không làm fail API
            e.printStackTrace();
        }

        return FloodSimulationRunResponse.builder()
                .simulationId(simId)
                .results(responseList)
                .build();
    }

    @Override
    public List<FloodStatisticDto> getSimulationStatistics(String simulationId) {
        return simulationRepository.getStatisticsBySimulationId(UUID.fromString(simulationId));
    }
}
