package com.example.GuardBatXat.controller.auth;
import com.example.GuardBatXat.entity.UserProfile;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.dto.request.auth.UserProfileRequest;
import com.example.GuardBatXat.dto.request.auth.UserUpdateRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.auth.UserProfileResponse;
import com.example.GuardBatXat.dto.response.auth.DemoLocationResponse;
import com.example.GuardBatXat.dto.response.auth.UserResponse;
import com.example.GuardBatXat.security.JwtService;
import com.example.GuardBatXat.service.DemoLocationService;
import com.example.GuardBatXat.service.UserService;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DemoLocationService demoLocationService;
    private final JwtService jwtService;

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

    @GetMapping("/me/demo-location")
    public ResponseEntity<ApiResponse<DemoLocationResponse>> getMyDemoLocation(
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtService.isDemoToken(token)) {
            throw new AppException(
                    ErrorCode.UNAUTHORIZED,
                    "Vị trí trình diễn chỉ khả dụng cho phiên đăng nhập bằng QR"
            );
        }

        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        DemoLocationResponse location = demoLocationService.getForIdentifier(identifier);
        return ResponseEntity.ok(ApiResponse.<DemoLocationResponse>builder()
                .code(200)
                .message("Lấy vị trí trình diễn thành công")
                .data(location)
                .build());
    }

}
