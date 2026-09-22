package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationRunResponse;
import com.example.GuardBatXat.websocket.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoFloodSimulationServiceTests {

    @Mock private RestTemplate restTemplate;
    @Mock private FloodSimulationService floodSimulationService;
    @Mock private CommanderDashboardService commanderDashboardService;
    @Mock private NotificationSender notificationSender;
    @Mock private DemoCitizenSnapshotService demoCitizenSnapshotService;

    private DemoFloodSimulationService service;

    @BeforeEach
    void setUp() {
        service = new DemoFloodSimulationService(
                restTemplate,
                floodSimulationService,
                commanderDashboardService,
                notificationSender,
                demoCitizenSnapshotService
        );
    }

    @Test
    void commanderScenarioPublishesClearlyMarkedCitizenDemo() {
        FloodSimulationResponse impact = FloodSimulationResponse.builder()
                .simulationId("sim-1")
                .buildingId(10L)
                .depth(1.2)
                .build();
        Map<String, Object> stats = Map.of(
                "nha_bi_ngap", 1,
                "nguoi_can_so_tan", 4,
                "duong_bi_chan", 2
        );
        when(floodSimulationService.runSimulation(83.5)).thenReturn(
                FloodSimulationRunResponse.builder()
                        .simulationId("sim-1")
                        .results(List.of(impact))
                        .build()
        );
        when(commanderDashboardService.getDashboardStats(83.5)).thenReturn(stats);
        when(demoCitizenSnapshotService.publish(any(), eq(stats), any()))
                .thenReturn(Map.of("revision", 123L));

        Map<String, Object> result = service.publishCommanderWaterLevelScenario(83.5);

        @SuppressWarnings("unchecked")
        Map<String, Object> scenario = (Map<String, Object>) result.get("scenario");
        @SuppressWarnings("unchecked")
        Map<String, Object> citizenDemo = (Map<String, Object>) result.get("citizenDemo");
        assertThat(scenario.get("riskLevel")).isEqualTo("DANGER");
        assertThat(scenario.get("stageSource")).isEqualTo("COMMANDER_SELECTED_WATER_LEVEL");
        assertThat(scenario.get("trainingEligible")).isEqualTo(false);
        assertThat(citizenDemo.get("scope")).isEqualTo("CITIZEN_PUBLIC_DEMO");
        verify(demoCitizenSnapshotService).publish(any(), eq(stats), eq(List.of(impact)));
    }
}
