package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.EvacuationResponse;
import com.example.GuardBatXat.dto.response.HeatmapProjection;
import com.example.GuardBatXat.dto.response.LocationCheckResponse;
import com.example.GuardBatXat.repository.HeatmapRepository;
import com.example.GuardBatXat.service.MapBroadcastService;
import com.example.GuardBatXat.service.RiskService;
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
    private final RiskService riskService;
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
    @PostMapping("/check-safety")
    public ResponseEntity<ApiResponse<LocationCheckResponse>> checkSafety(@RequestBody LocationCheckRequest request) {
        LocationCheckResponse response = riskService.checkLocationSafety(request);

        return ResponseEntity.ok(ApiResponse.<LocationCheckResponse>builder()
                .code(200)
                .message("Đã đánh giá thành công vị trí.")
                .data(response)
                .build());
    }
    @PostMapping("/evacuation-route")
    public ResponseEntity<ApiResponse<EvacuationResponse>> findEvacuationRoute(@RequestBody LocationCheckRequest request) {
        EvacuationResponse response = riskService.findNearestSafeHavens(request);

        return ResponseEntity.ok(ApiResponse.<EvacuationResponse>builder()
                .code(200)
                .message("Tìm điểm sơ tán thành công")
                .data(response)
                .build());
    }
}