package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationRunResponse;
import com.example.GuardBatXat.repository.FloodSimulationRepository;
import com.example.GuardBatXat.repository.projection.FloodSimulationMapPoint;
import com.example.GuardBatXat.service.impl.FloodSimulationServiceImpl;
import com.example.GuardBatXat.websocket.NotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FloodSimulationServiceImplTest {

    @Mock private FloodSimulationRepository simulationRepository;
    @Mock private NotificationSender notificationSender;

    @Test
    void runSimulationReturnsTheLightweightMapProjection() {
        FloodSimulationMapPoint point = new FloodSimulationMapPoint() {
            @Override public Long getBuildingId() { return 15L; }
            @Override public Double getDepth() { return 1.25; }
            @Override public String getStatus() { return "Nguy cơ cao"; }
            @Override public Double getLng() { return 103.8123; }
            @Override public Double getLat() { return 22.6512; }
        };
        when(simulationRepository.findMapPointsBySimulationId(any(UUID.class)))
                .thenReturn(List.of(point));

        FloodSimulationServiceImpl service = new FloodSimulationServiceImpl(
                simulationRepository, notificationSender
        );
        FloodSimulationRunResponse result = service.runSimulation(2.5);

        assertEquals(1, result.getResults().size());
        assertEquals(15L, result.getResults().get(0).getBuildingId());
        assertEquals(103.8123, result.getResults().get(0).getLng());
        assertEquals(22.6512, result.getResults().get(0).getLat());

        ArgumentCaptor<String> simulationId = ArgumentCaptor.forClass(String.class);
        verify(simulationRepository).executeFloodSimulationNative(simulationId.capture(), eq(2.5));
        assertEquals(result.getSimulationId(), simulationId.getValue());
        verify(notificationSender).sendSystemNotification(
                eq("/topic/simulation-results"), eq(result.getResults())
        );
    }
}
