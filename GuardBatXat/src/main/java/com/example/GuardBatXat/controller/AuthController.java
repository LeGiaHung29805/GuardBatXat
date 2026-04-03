package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.LoginRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Đây là 2 thành phần lõi của Spring Security để kiểm tra user và tạo token
    // (Nếu IDE báo đỏ ở JwtUtils, đừng lo, chúng ta sẽ xử lý nó ngay sau đây)
    private final AuthenticationManager authenticationManager;
    // private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {

        // 1. Kiểm tra tài khoản và mật khẩu
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. Lưu trạng thái đăng nhập vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo chuỗi JWT Token (Đang tạm comment để chờ file JwtUtils)
        // String jwt = jwtUtils.generateJwtToken(authentication);
        String jwt = "day_la_token_gia_lap_cho_den_khi_co_jwt_utils";

        // 4. Trả về Token cho Frontend
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Đăng nhập thành công!")
                .data(jwt)
                .build());
    }
}