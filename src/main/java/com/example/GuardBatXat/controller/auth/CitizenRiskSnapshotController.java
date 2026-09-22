package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.service.RiskSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citizen/risk-snapshots")
@RequiredArgsConstructor
public class CitizenRiskSnapshotController {

    private final RiskSnapshotService snapshotService;

    @GetMapping("/latest")
    public ResponseEntity<?> latest() {
        Map<String, Object> snapshot = snapshotService.getLatestCitizenSnapshot();
        return snapshot.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(snapshot);
    }

    @GetMapping("/{revision}")
    public ResponseEntity<Map<String, Object>> byRevision(@PathVariable long revision) {
        if (!snapshotService.isCitizenRevision(revision)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshotService.getSnapshot(revision));
    }

    @GetMapping("/{revision}/heatmap")
    public ResponseEntity<List<Map<String, Object>>> heatmap(@PathVariable long revision) {
        if (!snapshotService.isCitizenRevision(revision)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshotService.getHeatmap(revision));
    }
}
