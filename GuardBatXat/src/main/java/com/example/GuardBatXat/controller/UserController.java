package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.UserProfileRequest;
import com.example.GuardBatXat.dto.request.UserUpdateRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import com.example.GuardBatXat.dto.response.UserProfileResponse;
import com.example.GuardBatXat.dto.response.UserResponse;
import com.example.GuardBatXat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API Xem hồ sơ cá nhân
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

        // Giao việc cho Service
        UserResponse myProfile = userService.getMyProfile(identifier);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Lấy hồ sơ thành công")
                .data(myProfile)
                .build());
    }

    // API Cập nhật hồ sơ cá nhân
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMySurvivalProfile(@RequestBody UserProfileRequest request) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfileResponse updatedProfile = userService.updateMySurvivalProfile(identifier, request);

        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .message("Cập nhật hồ sơ sinh tồn thành công!")
                .data(updatedProfile)
                .build());
    }
    //API lấy hồ sơ cá nhân
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMySurvivalProfile() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfileResponse survivalProfile = userService.getMySurvivalProfile(identifier);

        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .message("Lấy hồ sơ sinh tồn thành công")
                .data(survivalProfile)
                .build());
    }

}