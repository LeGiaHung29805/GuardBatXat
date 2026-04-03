package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.BuildingRequest;
import com.example.GuardBatXat.dto.request.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.service.AdminSpatialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/spatial")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSpatialController {

    private final AdminSpatialService spatialService;

    @PostMapping("/buildings")
    public ResponseEntity<ApiResponse<Void>> addBuilding(@RequestBody @Valid BuildingRequest request) {
        spatialService.addBuilding(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Thêm nhà cửa thành công").build());
    }

    @PutMapping("/buildings/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBuilding(@PathVariable Long id, @RequestBody @Valid BuildingRequest request) {
        spatialService.updateBuilding(id, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Cập nhật nhà cửa thành công").build());
    }

    @DeleteMapping("/buildings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(@PathVariable Long id) {
        spatialService.deleteBuilding(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Đã xóa công trình").build());
    }

    @PostMapping("/roads")
    public ResponseEntity<ApiResponse<Void>> addRoadEdge(@RequestBody @Valid RoadEdgeRequest request) {
        spatialService.addRoadEdge(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Thêm tuyến đường thành công").build());
    }
}