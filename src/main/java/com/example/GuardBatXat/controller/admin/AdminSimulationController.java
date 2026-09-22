package com.example.GuardBatXat.controller.admin;
import com.example.GuardBatXat.dto.request.commander.FloodSimulationRequest;
import com.example.GuardBatXat.dto.request.commander.DemoFloodScenarioRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationResponse;
import com.example.GuardBatXat.dto.response.commander.FloodSimulationRunResponse;
import com.example.GuardBatXat.service.FloodSimulationService;
import com.example.GuardBatXat.service.DemoFloodSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.GuardBatXat.dto.response.commander.FloodStatisticDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/simulation")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSimulationController {

    private final FloodSimulationService simulationService;
    private final DemoFloodSimulationService demoSimulationService;

    @PostMapping("/flood")
    public ResponseEntity<ApiResponse<FloodSimulationRunResponse>> runFloodSimulation(
            @RequestBody @Valid FloodSimulationRequest request) {

        return ResponseEntity.ok(ApiResponse.<FloodSimulationRunResponse>builder()
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

    @GetMapping("/demo/data-quality")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDemoDataQuality() {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Đã kiểm toán dữ liệu lịch sử")
                .data(demoSimulationService.getDataQuality())
                .build());
    }

    @PostMapping("/demo/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetDemoHistory() {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Đã khôi phục dữ liệu demo từ lịch sử gốc")
                .data(demoSimulationService.resetDemoHistory())
                .build());
    }

    @PostMapping("/demo/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runDemoScenario(
            @RequestBody @Valid DemoFloodScenarioRequest request) {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Đã rải mưa và hoàn tất mô phỏng demo")
                .data(demoSimulationService.runScenario(request))
                .build());
    }

}
