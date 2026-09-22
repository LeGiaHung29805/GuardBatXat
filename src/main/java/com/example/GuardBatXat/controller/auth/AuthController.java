package com.example.GuardBatXat.controller.auth;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.dto.request.auth.DemoLoginRequest;
import com.example.GuardBatXat.dto.request.auth.LoginRequest;
import com.example.GuardBatXat.dto.request.auth.UserCreationRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.auth.UserResponse;
import com.example.GuardBatXat.security.JwtService;
import com.example.GuardBatXat.service.RedisCacheService;
import com.example.GuardBatXat.service.DemoLoginService;
import com.example.GuardBatXat.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // Đã đồng bộ với SecurityConfig
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RedisCacheService redisCacheService;
    private final DemoLoginService demoLoginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Đăng nhập thành công!")
                .data(jwt)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid UserCreationRequest request) {

        // Đăng ký công khai luôn là Citizen. Quyền đặc biệt chỉ do Admin cấp.
        request.setRoleName("CITIZEN");
        UserResponse newUser = userService.registerCitizen(request);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.")
                .data(newUser)
                .build());
    }

    @PostMapping("/demo-login")
    public ResponseEntity<ApiResponse<String>> authenticateWithDemoToken(
            @RequestBody @Valid DemoLoginRequest request
    ) {
        String jwt = demoLoginService.exchangeToken(request.getToken());
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Đăng nhập bằng mã QR thành công")
                .data(jwt)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            long remainingTime = jwtService.getRemainingTimeInMinutes(jwt);
            if (remainingTime > 0) {
                redisCacheService.setCache("blacklist:" + jwt, "logout", remainingTime);
            }
        }
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Đăng xuất thành công!")
                .build());
    }
}
