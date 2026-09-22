package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Snapshot cong khai, chi doc cho man hinh Citizen khi dang trinh dien.
 *
 * <p>Snapshot co TTL va khong dung chung bang snapshot REAL. Du lieu luon duoc
 * gan nhan DEMO de nguoi xem thay hau qua kich ban ma khong nham thanh canh bao
 * van hanh.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemoCitizenSnapshotService {

    private static final String LATEST_KEY = "demo:risk:latest";
    private static final String HEATMAP_KEY_PREFIX = "demo:risk:heatmap:";

    private final RedisCacheService redisCacheService;
    private final NotificationSender notificationSender;

    @Value("${batxat.risk.demo-ttl-minutes:240}")
    private long ttlMinutes;

    public Map<String, Object> publish(
            Map<String, Object> scenario,
            Map<String, Object> stats,
            List<FloodSimulationResponse> impacts
    ) {
        OffsetDateTime issuedAt = OffsetDateTime.now();
        long revision = System.currentTimeMillis();

        List<Map<String, Object>> heatmap = new ArrayList<>(impacts.size());
        for (FloodSimulationResponse impact : impacts) {
            if (impact.getLat() == null || impact.getLng() == null) continue;
            double depth = impact.getDepth() == null ? 0.0 : Math.max(0.0, impact.getDepth());
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("lat", impact.getLat());
            point.put("lng", impact.getLng());
            point.put("weight", Math.min(1.0, depth / 2.0));
            point.put("max_depth_m", depth);
            point.put("severity", impact.getStatus());
            point.put("building_id", impact.getBuildingId());
            point.put("population", impact.getPopulation());
            heatmap.add(point);
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("revision", revision);
        snapshot.put("mode", "DEMO");
        snapshot.put("publication_scope", "CITIZEN_PUBLIC_DEMO");
        snapshot.put("severity", String.valueOf(scenario.getOrDefault("riskLevel", "DEMO")));
        snapshot.put("issued_at", issuedAt);
        snapshot.put("expires_at", issuedAt.plusMinutes(ttlMinutes));
        snapshot.put("engine", "SCS_CN_V1+DEMO_STAGE_IMPACT");
        snapshot.put("is_calibrated", false);
        snapshot.put("scenario", scenario);
        snapshot.put("stats", stats);
        snapshot.put("impactCount", heatmap.size());
        snapshot.put("simulationId", impacts.isEmpty() ? null : impacts.get(0).getSimulationId());
        snapshot.put("disclaimer", "MO PHONG - KHONG PHAI CANH BAO VAN HANH");

        redisCacheService.setCache(LATEST_KEY, snapshot, ttlMinutes);
        redisCacheService.setCache(HEATMAP_KEY_PREFIX + revision, heatmap, ttlMinutes);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", "DEMO_RISK_SNAPSHOT");
        event.put("mode", "DEMO");
        event.put("revision", revision);
        event.put("severity", snapshot.get("severity"));
        event.put("snapshotUrl", "/api/v1/citizen/demo-snapshots/" + revision);
        event.put("demo", true);
        event.put("issuedAt", issuedAt);
        try {
            notificationSender.sendSystemNotification("/topic/citizen/demo-updates", event);
        } catch (Exception exception) {
            log.warn("Khong phat duoc snapshot demo toi Citizen: {}", exception.getMessage());
        }
        return snapshot;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getLatest() {
        Object value = redisCacheService.getCache(LATEST_KEY);
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    public Map<String, Object> getSnapshot(long revision) {
        Map<String, Object> snapshot = getLatest();
        Object current = snapshot.get("revision");
        if (!(current instanceof Number number) || number.longValue() != revision) {
            return Map.of();
        }
        return snapshot;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getHeatmap(long revision) {
        if (getSnapshot(revision).isEmpty()) return List.of();
        Object value = redisCacheService.getCache(HEATMAP_KEY_PREFIX + revision);
        return value instanceof List<?> ? (List<Map<String, Object>>) value : List.of();
    }

    public void clear() {
        Map<String, Object> current = getLatest();
        Object revision = current.get("revision");
        if (revision instanceof Number number) {
            redisCacheService.deleteCache(HEATMAP_KEY_PREFIX + number.longValue());
        }
        redisCacheService.deleteCache(LATEST_KEY);

        try {
            notificationSender.sendSystemNotification(
                    "/topic/citizen/demo-updates",
                    Map.of("eventType", "DEMO_RISK_RESET", "mode", "DEMO", "reset", true)
            );
        } catch (Exception exception) {
            log.warn("Khong phat duoc su kien reset demo: {}", exception.getMessage());
        }
    }

}
