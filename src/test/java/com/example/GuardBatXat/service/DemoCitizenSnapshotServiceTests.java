package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.websocket.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DemoCitizenSnapshotServiceTests {

    @Mock private RedisCacheService redisCacheService;
    @Mock private NotificationSender notificationSender;

    private DemoCitizenSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new DemoCitizenSnapshotService(redisCacheService, notificationSender);
        ReflectionTestUtils.setField(service, "ttlMinutes", 240L);
    }

    @Test
    void publishCreatesClearlyMarkedDemoSnapshotAndHeatmapEvent() {
        FloodSimulationResponse impact = FloodSimulationResponse.builder()
                .simulationId("93c30188-52c4-4f63-98c5-b80568e7d872")
                .buildingId(101L)
                .lat(22.61)
                .lng(103.72)
                .depth(2.4)
                .status("Nguy hiểm")
                .population(6)
                .build();

        Map<String, Object> snapshot = service.publish(
                Map.of("scenarioName", "Mua cuc doan", "riskLevel", "DANGER"),
                Map.of("nha_bi_ngap", 1, "nguoi_can_so_tan", 6, "duong_bi_chan", 2),
                List.of(impact)
        );

        assertThat(snapshot.get("mode")).isEqualTo("DEMO");
        assertThat(snapshot.get("publication_scope")).isEqualTo("CITIZEN_PUBLIC_DEMO");
        assertThat(snapshot.get("is_calibrated")).isEqualTo(false);
        assertThat(snapshot.get("impactCount")).isEqualTo(1);

        ArgumentCaptor<Object> heatmapCaptor = ArgumentCaptor.forClass(Object.class);
        long revision = ((Number) snapshot.get("revision")).longValue();
        verify(redisCacheService).setCache(
                eq("demo:risk:heatmap:" + revision), heatmapCaptor.capture(), eq(240L));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> heatmap = (List<Map<String, Object>>) heatmapCaptor.getValue();
        assertThat(heatmap).hasSize(1);
        assertThat(heatmap.get(0).get("weight")).isEqualTo(1.0);
        verify(notificationSender).sendSystemNotification(
                eq("/topic/citizen/demo-updates"), org.mockito.ArgumentMatchers.any());
    }

}
