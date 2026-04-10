package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.RoutingResponse;
import com.example.GuardBatXat.service.RoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/routing") // ĐƯỜNG DẪN ADMIN
@RequiredArgsConstructor
public class AdminRoutingController {

    private final RoutingService routingService;

    @PostMapping("/{strategyName}")
    public ResponseEntity<ApiResponse<RoutingResponse>> getRoute(
            @PathVariable String strategyName,
            @RequestBody @Valid RoutingRequest request) {

        return ResponseEntity.ok(ApiResponse.<RoutingResponse>builder()
                .code(200)
                .message("Tìm đường thành công theo chiến lược: " + strategyName)
                .data(routingService.findOptimalRoute(strategyName, request))
                .build());
    }
}