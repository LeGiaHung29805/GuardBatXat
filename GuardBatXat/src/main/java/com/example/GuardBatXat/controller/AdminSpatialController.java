package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.BuildingRequest;
import com.example.GuardBatXat.dto.request.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.RoadEdgeListDto;
import com.example.GuardBatXat.service.AdminSpatialService;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.RoadEdgeRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/spatial")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSpatialController {

    private final AdminSpatialService spatialService;
    private final BuildingRepository buildingRepository;
    private final RoadEdgeRepository roadEdgeRepository;

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

    // ==========================================
    // API GET ĐÃ ĐƯỢC XỬ LÝ LỖI STACKOVERFLOW
    // ==========================================

    @GetMapping("/buildings")
    public ResponseEntity<Map<String, Object>> getAllBuildings() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("code", 200);
            response.put("message", "Thành công");

            List<Map<String, Object>> safeData = new ArrayList<>();
            buildingRepository.findAll().forEach(b -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", b.getId());
                // Chú ý: Backend trả về buildingType, areaInMeters (chuẩn CamelCase để Front-end hứng)
                map.put("buildingType", b.getBuildingType());
                map.put("areaInMeters", b.getAreaInMeters());
                map.put("maxCapacity", b.getMaxCapacity());
                safeData.add(map);
            });

            response.put("data", safeData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Lỗi lấy dữ liệu nhà cửa: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/roads")
    public ResponseEntity<Map<String, Object>> getAllRoads() {
        Map<String, Object> response = new HashMap<>();
        try {
            // SỬ DỤNG HÀM TỐI ƯU TỪ SERVICE
            // Hàm này truy vấn thẳng ra DTO, KHÔNG LÔI CỘT 'geom' LÊN RAM SERVER!
            List<RoadEdgeListDto> optimizedData = spatialService.getAllRoadEdgesOptimized();

            response.put("code", 200);
            response.put("message", "Thành công");
            response.put("data", optimizedData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Lỗi lấy dữ liệu tuyến đường: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}