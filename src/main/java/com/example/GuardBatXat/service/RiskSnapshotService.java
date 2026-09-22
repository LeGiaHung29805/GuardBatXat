package com.example.GuardBatXat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskSnapshotService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> completeForecast(UUID forecastRunId, UUID hydraulicRunId) {
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                "SELECT revision FROM batxat_risk_snapshot WHERE forecast_run_id=?",
                forecastRunId
        );
        if (!existing.isEmpty()) {
            return getSnapshot(((Number) existing.get(0).get("revision")).longValue());
        }

        Map<String, Object> run = single("""
                SELECT run_id, mode, status, publication_scope, citizen_publishable,
                       issued_at, quality_status, quality_flags, source,
                       provider_model, raw_hash, model_version
                FROM batxat_forecast_run WHERE run_id=?
                """, forecastRunId);
        if (!"PUBLISHED".equals(run.get("status"))) {
            throw new IllegalStateException("Forecast run chưa ở trạng thái PUBLISHED");
        }
        Map<String, Object> hydraulic = single("""
                SELECT hydraulic_run_id, forecast_run_id, engine, status, valid_time,
                       stage_m, max_depth_m, flooded_area_m2, quality_flags, result_hash
                FROM batxat_hydraulic_run WHERE hydraulic_run_id=?
                """, hydraulicRunId);
        if (!forecastRunId.equals(hydraulic.get("forecast_run_id"))) {
            throw new IllegalStateException("Hydraulic run không thuộc forecast run");
        }
        if (!"VALIDATED".equals(hydraulic.get("status"))) {
            throw new IllegalStateException("HAND run chưa được VALIDATED");
        }

        Map<String, Object> critical = single("""
                SELECT valid_time, lead_hours, flood_risk_index, alert_level, method,
                       effective_rainfall_mm, peak_discharge_m3s, stage_preview_m,
                       anomaly_score, anomaly_threshold, anomaly_detected,
                       anomaly_model_version, recommended_action,
                       decision_confidence, decision_policy_version, decision_basis
                FROM batxat_risk_forecast WHERE run_id=?
                ORDER BY flood_risk_index DESC, lead_hours DESC LIMIT 1
                """, forecastRunId);
        OffsetDateTime validFrom = jdbcTemplate.queryForObject(
                "SELECT MIN(valid_time) FROM batxat_risk_forecast WHERE run_id=?",
                OffsetDateTime.class, forecastRunId
        );
        OffsetDateTime validUntil = jdbcTemplate.queryForObject(
                "SELECT MAX(valid_time) FROM batxat_risk_forecast WHERE run_id=?",
                OffsetDateTime.class, forecastRunId
        );
        Integer floodedBuildings = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM batxat_hydraulic_building_impact
                WHERE hydraulic_run_id=?
                """, Integer.class, hydraulicRunId);
        Integer affectedRoads = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM batxat_hydraulic_road_impact
                WHERE hydraulic_run_id=?
                """, Integer.class, hydraulicRunId);
        Integer blockedRoads = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM batxat_hydraulic_road_impact
                WHERE hydraulic_run_id=? AND status='BLOCKED'
                """, Integer.class, hydraulicRunId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("forecastRunId", forecastRunId);
        payload.put("hydraulicRunId", hydraulicRunId);
        payload.put("source", run.get("source"));
        payload.put("providerModel", run.get("provider_model"));
        payload.put("rawHash", run.get("raw_hash"));
        payload.put("modelVersion", run.get("model_version"));
        payload.put("qualityStatus", run.get("quality_status"));
        payload.put("qualityFlags", parseJson(run.get("quality_flags")));
        payload.put("riskIndex", critical.get("flood_risk_index"));
        payload.put("criticalLeadHours", critical.get("lead_hours"));
        payload.put("alertLevel", critical.get("alert_level"));
        payload.put("method", critical.get("method"));
        payload.put("effectiveRainfallMm", critical.get("effective_rainfall_mm"));
        payload.put("peakDischargeM3s", critical.get("peak_discharge_m3s"));
        payload.put("stagePreviewM", critical.get("stage_preview_m"));
        payload.put("anomalyScore", critical.get("anomaly_score"));
        payload.put("anomalyThreshold", critical.get("anomaly_threshold"));
        payload.put("anomalyDetected", critical.get("anomaly_detected"));
        payload.put("anomalyModelVersion", critical.get("anomaly_model_version"));
        payload.put("recommendedAction", critical.get("recommended_action"));
        payload.put("decisionConfidence", critical.get("decision_confidence"));
        payload.put("decisionPolicyVersion", critical.get("decision_policy_version"));
        payload.put("decisionBasis", parseJson(critical.get("decision_basis")));
        payload.put("directActuationAllowed", false);
        payload.put("engine", hydraulic.get("engine"));
        payload.put("hydraulicQualityFlags", parseJson(hydraulic.get("quality_flags")));
        payload.put("floodedBuildings", floodedBuildings);
        payload.put("affectedRoads", affectedRoads);
        payload.put("blockedRoads", blockedRoads);

        String mode = String.valueOf(run.get("mode"));
        String scope = String.valueOf(run.get("publication_scope"));
        boolean citizenPublishable = Boolean.TRUE.equals(run.get("citizen_publishable"));
        jdbcTemplate.update("UPDATE batxat_risk_snapshot SET is_active=FALSE WHERE mode=?", mode);
        Long revision = jdbcTemplate.queryForObject("""
                INSERT INTO batxat_risk_snapshot (
                    forecast_run_id, hydraulic_run_id, mode, publication_scope,
                    severity, issued_at, valid_from, valid_until, engine,
                    is_calibrated, is_active, expires_at, snapshot
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, TRUE, ?, CAST(? AS jsonb))
                RETURNING revision
                """, Long.class,
                forecastRunId, hydraulicRunId, mode, scope,
                critical.get("alert_level"), run.get("issued_at"), validFrom, validUntil,
                hydraulic.get("engine"), validUntil.plusHours(6), json(payload));

        String eventType = citizenPublishable
                ? "RISK_SNAPSHOT_PUBLISHED" : "RISK_PREVIEW_READY";
        String destination = citizenPublishable
                ? "/topic/citizen/risk-updates" : "/topic/commander/pipeline-status";
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID());
        event.put("eventType", eventType);
        event.put("mode", mode);
        event.put("revision", revision);
        event.put("forecastRunId", forecastRunId);
        event.put("hydraulicRunId", hydraulicRunId);
        event.put("issuedAt", run.get("issued_at"));
        event.put("validFrom", validFrom);
        event.put("validUntil", validUntil);
        event.put("severity", critical.get("alert_level"));
        event.put("scope", "BAT_XAT_MOI_5_AREAS");
        event.put("snapshotUrl", "/api/v1/citizen/risk-snapshots/" + revision);
        event.put("citizenPublishable", citizenPublishable);

        jdbcTemplate.update("""
                INSERT INTO batxat_risk_event_outbox
                    (event_id, revision, event_type, destination, payload)
                VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                """, event.get("eventId"), revision, eventType, destination, json(event));
        return getSnapshot(revision);
    }

    public Map<String, Object> getLatestCitizenSnapshot() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT revision FROM batxat_risk_snapshot
                WHERE mode='REAL' AND publication_scope='CITIZEN'
                  AND is_active=TRUE AND expires_at>NOW()
                ORDER BY revision DESC LIMIT 1
                """);
        return rows.isEmpty() ? Map.of() : getSnapshot(((Number) rows.get(0).get("revision")).longValue());
    }

    public Map<String, Object> getSnapshot(long revision) {
        Map<String, Object> row = single("""
                SELECT revision, forecast_run_id, hydraulic_run_id, mode,
                       publication_scope, severity, issued_at, valid_from,
                       valid_until, geographic_scope, engine, is_calibrated,
                       is_active, expires_at, snapshot
                FROM batxat_risk_snapshot WHERE revision=?
                """, revision);
        row.put("snapshot", parseJson(row.get("snapshot")));
        row.put("heatmapUrl", "/api/v1/citizen/risk-snapshots/" + revision + "/heatmap");
        return row;
    }

    public boolean isCitizenRevision(long revision) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM batxat_risk_snapshot
                WHERE revision=? AND publication_scope='CITIZEN'
                  AND mode='REAL' AND expires_at>NOW()
                """, Integer.class, revision);
        return count != null && count == 1;
    }

    public List<Map<String, Object>> getHeatmap(long revision) {
        return jdbcTemplate.queryForList("""
                SELECT ST_Y(ST_PointOnSurface(ST_Transform(b.geom, 4326))) AS lat,
                       ST_X(ST_PointOnSurface(ST_Transform(b.geom, 4326))) AS lng,
                       LEAST(1.0, i.max_depth_m / 2.0) AS weight,
                       i.max_depth_m, i.severity
                FROM batxat_risk_snapshot s
                JOIN batxat_hydraulic_building_impact i
                  ON i.hydraulic_run_id=s.hydraulic_run_id
                JOIN batxat_buildings b ON b.id=i.building_id
                WHERE s.revision=?
                """, revision);
    }

    private Map<String, Object> single(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        if (rows.size() != 1) {
            throw new IllegalArgumentException("Không tìm thấy đúng một bản ghi cần thiết");
        }
        return new LinkedHashMap<>(rows.get(0));
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không tuần tự hóa được snapshot", exception);
        }
    }

    private Object parseJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(String.valueOf(value), Object.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("JSONB trong CSDL không hợp lệ", exception);
        }
    }
}
