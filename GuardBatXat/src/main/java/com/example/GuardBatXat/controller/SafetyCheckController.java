package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.LocationCheckResponse;
import com.example.GuardBatXat.service.SafetyCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/safety")
@RequiredArgsConstructor
public class SafetyCheckController {

    private final SafetyCheckService safetyCheckService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<LocationCheckResponse>> checkLocation(
            @RequestBody LocationCheckRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(
                200, "Success", safetyCheckService.evaluateLocationSafety(request)
        ));
    }
}