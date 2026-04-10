package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.RoutingCompareResponse;
import com.example.GuardBatXat.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/routing")
@RequiredArgsConstructor
public class AdminRoutingController {

    private final RoutingService routingService;

    // ==========================================
    // API TÌM ĐƯỜNG ADMIN (LẤY 3 LỘ TRÌNH CÙNG LÚC)
    // ==========================================
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<RoutingCompareResponse>> compareRoutes(@RequestBody RoutingRequest request) {

        RoutingCompareResponse data = routingService.findAdminCompareRoute(request);

        return ResponseEntity.ok(ApiResponse.<RoutingCompareResponse>builder()
                .code(200)
                .message("Lấy dữ liệu đối chiếu 3 lộ trình thành công")
                .data(data)
                .build());
    }
}