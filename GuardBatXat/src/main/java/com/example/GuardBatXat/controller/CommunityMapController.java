package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.response.HeatmapProjection;
import com.example.GuardBatXat.repository.HeatmapRepository;
import com.example.GuardBatXat.service.MapBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityMapController {

    @Autowired
    private HeatmapRepository heatmapRepository;
    @Autowired
    private MapBroadcastService broadcastService;
    @GetMapping("/heatmap/landslide")
    public ResponseEntity<List<HeatmapProjection>> getInitialLandslideHeatmap() {
        return ResponseEntity.ok(heatmapRepository.getLandslideHeatmap());
    }

    @PostMapping("/internal/trigger-broadcast")
    public ResponseEntity<String> triggerFromPython() {
        broadcastService.broadcastNewLandslideHeatmap();
        return ResponseEntity.ok("Spring Boot đã phát sóng bản đồ Sạt lở thành công!");
    }
}