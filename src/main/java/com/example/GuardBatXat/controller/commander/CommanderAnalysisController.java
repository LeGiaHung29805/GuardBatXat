package com.example.GuardBatXat.controller.commander;

import com.example.GuardBatXat.service.CommanderAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commander/analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommanderAnalysisController {

    private final CommanderAnalysisService analysisService;

    @GetMapping("/damage-by-type")
    public ResponseEntity<List<Map<String, Object>>> getDamageByType(
            @RequestParam(value = "level", defaultValue = "80") Double level) {
        return ResponseEntity.ok(analysisService.getDamageByType(level));
    }

    @GetMapping("/severity-chart")
    public ResponseEntity<List<Map<String, Object>>> getSeverityChart(
            @RequestParam(value = "level", defaultValue = "80") Double level) {
        return ResponseEntity.ok(analysisService.getSeverityChart(level));
    }

    @GetMapping("/damage-trend")
    public ResponseEntity<List<Map<String, Object>>> getDamageTrend() {
        return ResponseEntity.ok(analysisService.getDamageTrend());
    }

    @GetMapping("/top-areas")
    public ResponseEntity<List<Map<String, Object>>> getTopAreas(
            @RequestParam(value = "level", defaultValue = "80") Double level) {
        return ResponseEntity.ok(analysisService.getTopAreas(level));
    }

    @GetMapping("/commune-ranking")
    public ResponseEntity<List<Map<String, Object>>> getCommuneRanking(
            @RequestParam(value = "level", defaultValue = "80") Double level) {
        return ResponseEntity.ok(analysisService.getCommuneRanking(level));
    }

    @GetMapping("/water-forecast")
    public ResponseEntity<List<Map<String, Object>>> getWaterLevelForecast() {
        return ResponseEntity.ok(analysisService.getWaterLevelForecast());
    }
}
