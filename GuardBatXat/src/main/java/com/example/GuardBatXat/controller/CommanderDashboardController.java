package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.CommanderLandslideProjection;
import com.example.GuardBatXat.service.CommanderDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commander/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommanderDashboardController {

    private final CommanderDashboardService commanderDashboardService;

    @GetMapping("/commander-heatmap-flood")
    public ResponseEntity<ApiResponse<List<CommanderFloodProjection>>> getCommanderFloodHeatmap(
            @RequestParam(value = "level", defaultValue = "80") Double level) {
        try {
            List<CommanderFloodProjection> data = commanderDashboardService.getCommanderFloodHeatmap(level);
            return ResponseEntity.ok(ApiResponse.<List<CommanderFloodProjection>>builder()
                    .code(200)
                    .message("Lấy dữ liệu ngập lụt thành công")
                    .data(data)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<List<CommanderFloodProjection>>builder()
                    .code(500)
                    .message("Error: " + e.getMessage() + " | " + e.getCause())
                    .build());
        }
    }

    @GetMapping("/commander-heatmap-landslide")
    public ResponseEntity<ApiResponse<List<CommanderLandslideProjection>>> getCommanderLandslideHeatmap() {
        
        List<CommanderLandslideProjection> data = commanderDashboardService.getCommanderLandslideHeatmap();

        return ResponseEntity.ok(ApiResponse.<List<CommanderLandslideProjection>>builder()
                .code(200)
                .message("Lấy dữ liệu sạt lở thành công")
                .data(data)
                .build());
    }
}
