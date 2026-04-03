package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
public class RoutingController {

    private final RoutingService routingService;

    @PostMapping("/safe-route")
    public ResponseEntity<ApiResponse<Object>> findSafeRoute(@RequestBody RoutingRequest request) {

        // Giao việc cho Service gọi sang Python
        Object aiResponse = routingService.getSafeRouteFromAI(request);

        // Đóng gói lại theo chuẩn ApiResponse của dự án
        ApiResponse<Object> response = new ApiResponse<>(
                200,
                "Success",
                aiResponse
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/find-safe-shelter")
    public ResponseEntity<ApiResponse<Object>> findSafeShelter(@RequestBody FindShelterRequest request) {
        Object aiResponse = routingService.findSafeShelterFromAI(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", aiResponse));
    }
}