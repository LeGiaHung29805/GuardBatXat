package com.example.GuardBatXat.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forecast")
@RequiredArgsConstructor
public class ForecastOperationalController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/latest")
    public ResponseEntity<?> latest() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT run_id, mode, source, provider_model, issued_at,
                       horizon_hours, status, quality_status, quality_flags,
                       raw_hash, model_version, publication_scope,
                       citizen_publishable, started_at, finished_at
                FROM batxat_forecast_run
                WHERE mode='REAL' AND is_active=TRUE
                ORDER BY issued_at DESC LIMIT 1
                """);
        return rows.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(rows.get(0));
    }

    @GetMapping("/latest/timeline")
    public List<Map<String, Object>> latestTimeline() {
        return jdbcTemplate.queryForList("""
                SELECT r.valid_time, r.lead_hours, r.flood_risk_index,
                       r.calibrated_probability, r.effective_rainfall_mm,
                       r.peak_discharge_m3s, r.stage_preview_m,
                       r.alert_level, r.method, r.explanation,
                       r.anomaly_score, r.anomaly_threshold,
                       r.anomaly_detected, r.anomaly_model_version,
                       r.recommended_action, r.decision_confidence,
                       r.decision_policy_version, r.decision_basis
                FROM batxat_risk_forecast r
                JOIN batxat_forecast_run f USING(run_id)
                WHERE f.mode='REAL' AND f.is_active=TRUE
                ORDER BY r.lead_hours
                """);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<?> run(@PathVariable UUID runId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM batxat_forecast_run WHERE run_id=?", runId);
        return rows.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(rows.get(0));
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        List<Map<String, Object>> active = jdbcTemplate.queryForList("""
                SELECT run_id, issued_at, finished_at, quality_status,
                       publication_scope, citizen_publishable,
                       EXTRACT(EPOCH FROM (NOW()-issued_at))/3600.0 AS age_hours
                FROM batxat_forecast_run
                WHERE mode='REAL' AND is_active=TRUE LIMIT 1
                """);
        List<Map<String, Object>> failed = jdbcTemplate.queryForList("""
                SELECT run_id, finished_at, error_message
                FROM batxat_forecast_run WHERE status='FAILED'
                ORDER BY finished_at DESC LIMIT 1
                """);
        return Map.of(
                "status", active.isEmpty() ? "NO_ACTIVE_RUN" : "OK",
                "activeRun", active.isEmpty() ? Map.of() : active.get(0),
                "lastFailure", failed.isEmpty() ? Map.of() : failed.get(0)
        );
    }
}
