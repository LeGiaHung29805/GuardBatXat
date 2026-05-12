package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.SosResponse;
import com.example.GuardBatXat.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rescue/sos")
@RequiredArgsConstructor
public class RescueSosController {

    private final SosService sosService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SosResponse>>> getAllSosRequests() {
        List<SosResponse> data = sosService.getAllSosRequests();
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách SOS thành công", data));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<String>> acceptSosRequest(@PathVariable Integer id) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        sosService.acceptSosRequest(id, identifier);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã nhận nhiệm vụ cứu hộ", null));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<String>> completeSosRequest(@PathVariable Integer id) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        sosService.completeSosRequest(id, identifier);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đã hoàn thành nhiệm vụ cứu hộ", null));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<ApiResponse<List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse>>> getSosUpdates(@PathVariable Integer id) {
        List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse> data = sosService.getSosUpdates(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy lịch sử cập nhật thành công", data));
    }

    @PostMapping("/{id}/updates")
    public ResponseEntity<ApiResponse<String>> addSosUpdate(
            @PathVariable Integer id,
            @RequestBody com.example.GuardBatXat.dto.request.SosUpdateLogRequest request) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        sosService.addSosUpdate(id, request, identifier);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thêm lịch sử cập nhật thành công", null));
    }
}
