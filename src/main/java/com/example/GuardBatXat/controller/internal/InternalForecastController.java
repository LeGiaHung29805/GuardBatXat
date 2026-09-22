package com.example.GuardBatXat.controller.internal;

import com.example.GuardBatXat.service.RiskSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/forecast-runs")
@RequiredArgsConstructor
public class InternalForecastController {

    private final RiskSnapshotService snapshotService;

    @Value("${batxat.internal.callback-token:}")
    private String callbackToken;

    @PostMapping("/{runId}/complete")
    public ResponseEntity<?> complete(
            @PathVariable UUID runId,
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody Map<String, Object> body
    ) {
        if (!validToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Internal token không hợp lệ"));
        }
        UUID bodyRunId = UUID.fromString(String.valueOf(body.get("forecastRunId")));
        UUID hydraulicRunId = UUID.fromString(String.valueOf(body.get("hydraulicRunId")));
        if (!runId.equals(bodyRunId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "runId không khớp payload"));
        }
        return ResponseEntity.ok(snapshotService.completeForecast(runId, hydraulicRunId));
    }

    private boolean validToken(String candidate) {
        if (callbackToken == null || callbackToken.isBlank() || candidate == null) {
            return false;
        }
        return MessageDigest.isEqual(
                callbackToken.getBytes(StandardCharsets.UTF_8),
                candidate.getBytes(StandardCharsets.UTF_8)
        );
    }
}
