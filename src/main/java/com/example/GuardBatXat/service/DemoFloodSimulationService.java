package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.commander.DemoFloodScenarioRequest;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoFloodSimulationService {

    @Value("${batxat.ai.service.base-url:http://localhost:5000}")
    private String aiServiceBaseUrl;

    private final RestTemplate restTemplate;
    private final FloodSimulationService floodSimulationService;
    private final CommanderDashboardService commanderDashboardService;
    private final NotificationSender notificationSender;
    private final DemoCitizenSnapshotService demoCitizenSnapshotService;

    public Map<String, Object> getDataQuality() {
        Map<String, Object> body = restTemplate.getForObject(
                aiServiceBaseUrl + "/api/v1/ai/demo/data-quality",
                Map.class
        );
        return extractData(body, "Không đọc được báo cáo chất lượng dữ liệu");
    }

    public Map<String, Object> resetDemoHistory() {
        Map<String, Object> body = restTemplate.postForObject(
                aiServiceBaseUrl + "/api/v1/ai/demo/reset",
                Map.of(),
                Map.class
        );
        Map<String, Object> result = extractData(body, "Không khôi phục được dữ liệu demo");
        demoCitizenSnapshotService.clear();
        return result;
    }

    public Map<String, Object> runScenario(DemoFloodScenarioRequest request) {
        try {
            Map<String, Object> body = restTemplate.postForObject(
                    aiServiceBaseUrl + "/api/v1/ai/demo/scenarios",
                    request,
                    Map.class
            );
            Map<String, Object> scenario = extractData(body, "AI không tạo được kịch bản mưa");
            Object rawLevel = scenario.get("simulatedFloodLevel");
            if (!(rawLevel instanceof Number number)) {
                throw new IllegalStateException("Kịch bản không trả về cao độ nước mô phỏng");
            }

            double waterLevel = number.doubleValue();
            List<FloodSimulationResponse> impacts = floodSimulationService.runSimulation(waterLevel);
            Map<String, Object> stats = commanderDashboardService.getDashboardStats(waterLevel);

            Map<String, Object> simulation = new LinkedHashMap<>();
            simulation.put("waterLevel", waterLevel);
            simulation.put("stats", stats);
            simulation.put("impacts", impacts);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("scenario", scenario);
            result.put("simulation", simulation);

            Map<String, Object> citizenSnapshot = demoCitizenSnapshotService.publish(
                    scenario, stats, impacts);
            result.put("citizenDemo", Map.of(
                    "revision", citizenSnapshot.get("revision"),
                    "topic", "/topic/citizen/demo-updates",
                    "scope", "CITIZEN_PUBLIC_DEMO"
            ));

            try {
                notificationSender.sendSystemNotification("/topic/demo-simulation", result);
            } catch (Exception websocketError) {
                log.warn("Không phát được frame demo qua WebSocket: {}", websocketError.getMessage());
            }
            return result;
        } catch (RestClientResponseException exception) {
            log.error("AI demo trả lỗi {}: {}", exception.getStatusCode(), exception.getResponseBodyAsString());
            throw new RuntimeException("Dịch vụ dữ liệu demo từ chối yêu cầu: " + exception.getResponseBodyAsString());
        }
    }

    public Map<String, Object> publishCommanderWaterLevelScenario(double waterLevel) {
        if (!Double.isFinite(waterLevel) || waterLevel < 0 || waterLevel > 2000) {
            throw new IllegalArgumentException("Cao độ mô phỏng không hợp lệ");
        }

        List<FloodSimulationResponse> impacts = floodSimulationService.runSimulation(waterLevel);
        Map<String, Object> stats = commanderDashboardService.getDashboardStats(waterLevel);

        Map<String, Object> scenario = new LinkedHashMap<>();
        scenario.put("scenarioId", UUID.randomUUID().toString());
        scenario.put("scenarioName", "Kịch bản Commander " + waterLevel + " m");
        scenario.put("hydrologyEngine", "MANUAL_STAGE_SCENARIO");
        scenario.put("stageSource", "COMMANDER_SELECTED_WATER_LEVEL");
        scenario.put("simulatedFloodLevel", waterLevel);
        scenario.put("riskLevel", commanderRiskLevel(waterLevel));
        scenario.put("trainingEligible", false);
        scenario.put("disclaimer", "MÔ PHỎNG COMMANDER - KHÔNG PHẢI CẢNH BÁO VẬN HÀNH");

        Map<String, Object> simulation = new LinkedHashMap<>();
        simulation.put("waterLevel", waterLevel);
        simulation.put("stats", stats);
        simulation.put("impacts", impacts);

        Map<String, Object> citizenSnapshot = demoCitizenSnapshotService.publish(
                scenario, stats, impacts);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", scenario);
        result.put("simulation", simulation);
        result.put("citizenDemo", Map.of(
                "revision", citizenSnapshot.get("revision"),
                "topic", "/topic/citizen/demo-updates",
                "scope", "CITIZEN_PUBLIC_DEMO"
        ));
        return result;
    }

    private String commanderRiskLevel(double waterLevel) {
        if (waterLevel >= 83.5) return "DANGER";
        if (waterLevel >= 82.0) return "WARNING";
        if (waterLevel >= 80.0) return "WATCH";
        return "NORMAL";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> body, String errorMessage) {
        if (body == null || !"success".equals(body.get("status")) || !(body.get("data") instanceof Map)) {
            throw new RuntimeException(errorMessage);
        }
        return (Map<String, Object>) body.get("data");
    }
}
