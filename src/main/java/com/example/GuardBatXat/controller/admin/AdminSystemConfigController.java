package com.example.GuardBatXat.controller.admin;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.ModelRegistry;
import com.example.GuardBatXat.entity.AhpWeight;

import com.example.GuardBatXat.dto.request.admin.AhpWeightRequest;
import com.example.GuardBatXat.dto.response.admin.AhpWeightResponse;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.admin.ModelRegistryResponse;
import com.example.GuardBatXat.service.AdminSystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSystemConfigController {

    private final AdminSystemConfigService systemService;

    // Lấy danh sách toàn bộ các phiên bản AI
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<ModelRegistryResponse>>> getAllModels() {
        return ResponseEntity.ok(ApiResponse.<List<ModelRegistryResponse>>builder()
                .code(200)
                .message("Lấy danh sách AI thành công")
                .data(systemService.getAllModels())
                .build());
    }

    // Đổi AI (Registry) đang hoạt động
    @PutMapping("/models/{id}/activate")
    public ResponseEntity<ApiResponse<ModelRegistryResponse>> activateModel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.<ModelRegistryResponse>builder()
                .code(200)
                .message("Đã kích hoạt mô hình AI mới")
                .data(systemService.activateModel(id))
                .build());
    }

    // Cập nhật trọng số AHP cho kịch bản (ví dụ: "safety" hoặc "rescue")
    @PutMapping("/weights/{strategyName}")
    public ResponseEntity<ApiResponse<AhpWeightResponse>> updateWeights(
            @PathVariable String strategyName,
            @RequestBody @Valid AhpWeightRequest request) {

        return ResponseEntity.ok(ApiResponse.<AhpWeightResponse>builder()
                .code(200)
                .message("Cập nhật trọng số thành công")
                .data(systemService.updateAhpWeights(strategyName, request))
                .build());
    }
}