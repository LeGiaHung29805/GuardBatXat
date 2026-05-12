package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.FindShelterRequest;
import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.RoutingResponse;
import com.example.GuardBatXat.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
public class AuthRoutingController {

    private final RoutingService routingService;

    @PostMapping("/find-safe-shelter")
    public ResponseEntity<ApiResponse<Object>> findSafeShelter(@RequestBody FindShelterRequest request) {
        Object aiResponse = routingService.findSafeShelterFromAI(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", aiResponse));
    }
    @PostMapping("/safe-route")
    public ResponseEntity<ApiResponse<Object>> getSafeRoute(@RequestBody RoutingRequest request) {
        Object aiResponse = routingService.getSafeRouteFromAI(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tìm đường thành công", aiResponse));
    }
}