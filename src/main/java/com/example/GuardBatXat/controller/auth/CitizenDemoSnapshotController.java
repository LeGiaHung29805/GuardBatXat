package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.service.DemoCitizenSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citizen/demo-snapshots")
@RequiredArgsConstructor
public class CitizenDemoSnapshotController {

    private final DemoCitizenSnapshotService snapshotService;

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> latest() {
        Map<String, Object> snapshot = snapshotService.getLatest();
        return snapshot.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(snapshot);
    }

    @GetMapping("/{revision}")
    public ResponseEntity<Map<String, Object>> byRevision(@PathVariable long revision) {
        Map<String, Object> snapshot = snapshotService.getSnapshot(revision);
        return snapshot.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(snapshot);
    }

    @GetMapping("/{revision}/heatmap")
    public ResponseEntity<List<Map<String, Object>>> heatmap(@PathVariable long revision) {
        if (snapshotService.getSnapshot(revision).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshotService.getHeatmap(revision));
    }
}
