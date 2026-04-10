package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.FloodSimulationRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.FloodSimulationResponse;
import com.example.GuardBatXat.service.FloodSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.GuardBatXat.dto.response.FloodStatisticDto;

import java.util.List;

@RestController
@RequestMapping("/api/admin/simulation")
@RequiredArgsConstructor
public class AdminSimulationController {

    private final FloodSimulationService simulationService;

    @PostMapping("/flood")
    public ResponseEntity<ApiResponse<List<FloodSimulationResponse>>> runFloodSimulation(
            @RequestBody @Valid FloodSimulationRequest request) {

        return ResponseEntity.ok(ApiResponse.<List<FloodSimulationResponse>>builder()
                .code(200)
                .message("Chạy mô phỏng ngập lụt hoàn tất")
                .data(simulationService.runSimulation(request.getWaterLevel()))
                .build());
    }

    @GetMapping("/flood/{simulationId}/stats")
    public ResponseEntity<ApiResponse<List<FloodStatisticDto>>> getSimulationStats(
            @PathVariable("simulationId") String simulationId) {

        return ResponseEntity.ok(ApiResponse.<List<FloodStatisticDto>>builder()
                .code(200)
                .message("Lấy thống kê thiệt hại thành công")
                .data(simulationService.getSimulationStatistics(simulationId))
                .build());
    }

}