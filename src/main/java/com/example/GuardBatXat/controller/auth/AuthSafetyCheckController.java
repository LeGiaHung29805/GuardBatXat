package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;
import com.example.GuardBatXat.service.SafetyCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.GuardBatXat.dto.response.rescue.NeighborhoodSafetyResponse;

@RestController
@RequestMapping("/api/v1/safety")
@RequiredArgsConstructor
public class AuthSafetyCheckController {

    private final SafetyCheckService safetyCheckService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<LocationCheckResponse>> checkLocation(
            @RequestBody LocationCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(
                200, "Success", safetyCheckService.evaluateLocationSafety(request)
        ));
    }

    @PostMapping("/neighborhood")
    public ResponseEntity<ApiResponse<NeighborhoodSafetyResponse>> checkNeighborhood(
            @RequestBody LocationCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(
                200, "Success", safetyCheckService.evaluateNeighborhoodSafety(request)
        ));
    }

    @GetMapping("/neighborhood")
    public ResponseEntity<ApiResponse<NeighborhoodSafetyResponse>> checkNeighborhoodGet(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "waterLevel", required = false) Double waterLevel) {
        LocationCheckRequest request = LocationCheckRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .waterLevel(waterLevel)
                .build();
        return ResponseEntity.ok(new ApiResponse<>(
                200, "Success", safetyCheckService.evaluateNeighborhoodSafety(request)
        ));
    }
}