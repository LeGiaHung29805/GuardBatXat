package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.entity.SosEntity;
import com.example.GuardBatXat.service.RescueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rescue")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RescueController {

    private final RescueService rescueService;

    @GetMapping("/sos")
    public ResponseEntity<ApiResponse<List<SosEntity>>> getRescueSosRequests() {
        List<SosEntity> list = rescueService.getAllSosRequests();
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", list));
    }

    @PutMapping("/sos/{id}/accept")
    public ResponseEntity<ApiResponse<SosEntity>> acceptRescueSosRequest(@PathVariable Integer id) {
        SosEntity updated = rescueService.acceptSosRequest(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã tiếp nhận cứu hộ", updated));
    }

    @PutMapping("/sos/{id}/complete")
    public ResponseEntity<ApiResponse<SosEntity>> completeRescueSosRequest(@PathVariable Integer id) {
        SosEntity updated = rescueService.completeSosRequest(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã hoàn thành cứu hộ", updated));
    }

    @GetMapping("/sos/{id}/updates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSosFieldUpdates(@PathVariable Integer id) {
        List<Map<String, Object>> updates = rescueService.getSosFieldUpdates(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", updates));
    }

    @PostMapping("/sos/{id}/updates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendSosFieldUpdate(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        Map<String, Object> update = rescueService.sendSosFieldUpdate(id, data);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã gửi cập nhật hiện trường", update));
    }
}
