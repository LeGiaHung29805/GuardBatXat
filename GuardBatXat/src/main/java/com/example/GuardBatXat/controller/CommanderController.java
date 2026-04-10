package com.example.GuardBatXat.controller;


import com.example.GuardBatXat.dto.request.AlertRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.repository.DisasterCommandRepository;
import com.example.GuardBatXat.service.CommanderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commander")
// @PreAuthorize("hasRole('COMMANDER')") // Bỏ comment nếu đã cài Spring Security
public class CommanderController {

    @Autowired
    private CommanderService commanderService;
    @Autowired
    private RestClient.Builder builder;
//    @Autowired
//    private DisasterCommandRepository disasterCommandRepository;

    // API: Xem Heatmap ngập lụt
    @GetMapping("/heatmap/flood")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFloodHeatmap(@RequestParam(defaultValue = "80.0") BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getFloodRiskData(level))
                .build());
    }

    // API: Xem Heatmap sạt lở
    @GetMapping("/heatmap/landslide")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLandslideHeatmap() {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getLandslideRiskData())
                .build());
    }

    // API: Kích hoạt kịch bản (80m, 82m, 83.5m...)
    @PostMapping("/scenario/trigger")
    public ResponseEntity<ApiResponse<Map<String, String>>> triggerScenario(@RequestParam BigDecimal floodLevel) {
        String simId = commanderService.triggerEvacuationScenario(floodLevel);
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("Đã chạy kịch bản thành công")
                .data(Map.of("simulationId", simId))
                .build());
    }

    // API: Phân tích thiệt hại sau kịch bản
    @GetMapping("/scenario/analysis/{simId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnalysis(@PathVariable String simId) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getPostDisasterAnalysis(simId))
                .build());
    }

    // API: Phát thông báo khẩn
    @PostMapping("/alert/broadcast")
    public ResponseEntity<ApiResponse<String>> broadcastAlert(@RequestBody AlertRequest request) {
        commanderService.broadcastEmergencyAlert(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .data("Đã phát tín hiệu báo động khẩn cấp tới khu vực!")
                .build());
    }

    // API: Lấy thống kê tổng quan (Dashboard) theo mực nước
    @GetMapping("/statistics/flood")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFloodStatistics(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getFloodStatistics(level))
                .build());
    }



    // API: Phục vụ riêng cho Dashboard hiển thị 4 ô thống kê
    @GetMapping("/statistics/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getDashboardStatistics(level))
                .build());
    }





    // API: Nút Đỏ - Phát lệnh sơ tán ngay
    @PostMapping("/evacuation/activate")
    public ResponseEntity<ApiResponse<String>> activateEvacuationOrder(@RequestParam BigDecimal level) {
        // Gọi service đẩy thông báo qua mạng
        commanderService.activateEvacuation(level);

        // Trả về cho màn hình Ban chỉ huy
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Success")
                .data("Đã phát lệnh báo động khẩn cấp cho kịch bản " + level + "m!")
                .build());
    }

    // API: Data Biểu đồ mức độ nghiêm trọng
    @GetMapping("/analysis/severity-chart")
    public ResponseEntity<ApiResponse<List<Map<String , Object>>>> getSeverityChart(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String,Object>>>builder()
                        .code(200)
                        .message("Success")
                .data(commanderService.getSeverityChartData(level))
                .build());
    }

    // API: Data Top 5 Cụm dân cư ngập nặng nhất
    @GetMapping("/analysis/top-areas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopAreas(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getTopAffectedAreas(level))
                .build());
    }



    // API: Biểu đồ đường (Line Chart) theo thời gian
    @GetMapping("/analysis/damage-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDamageTrend() {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getDamageTrendChartData())
                .build());
    }

    // API: Dữ liệu Biểu đồ Thiệt hại theo loại
    @GetMapping("/analysis/damage-by-type")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDamageByType(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getDamageByTypeData(level))
                .build());
    }

    // Nút Gửi Cảnh báo
    @PostMapping("/notifications/send")
    public ResponseEntity<ApiResponse<String>> sendNotification(@RequestBody Map<String, String> payload) {
        try {
            commanderService.sendAndSaveNotification(payload);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(200)
                    .message("Đã phát cảnh báo thành công!")
                    .data(null) // API này chỉ thực thi lệnh, không cần trả data
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .code(400)
                    .message("Lỗi: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    // Danh sách Lịch sử cảnh báo
    @GetMapping("/notifications/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotificationHistory() {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getNotificationHistory())
                .build());
    }


// 1.B NHÓM API HEATMAP CHO CHỈ HUY (DỮ LIỆU SÂU)

    // API: Heatmap Ngập lụt (Chi tiết cao cho Ban chỉ huy)
    @GetMapping("/dashboard/commander-heatmap-flood")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCommanderDetailedFloodHeatmap(@RequestParam BigDecimal level) {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getCommanderFloodHeatmapData(level))
                .build());
    }

    // API: Heatmap Sạt lở (Chi tiết cao cho Ban chỉ huy)
    @GetMapping("/dashboard/commander-heatmap-landslide")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCommanderDetailedLandslideHeatmap() {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getCommanderLandslideHeatmapData())
                .build());
    }

    @GetMapping("/spatial-data")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSpatialMapData() {
        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Success")
                .data(commanderService.getSpatialMapData())
                .build());
    }
}