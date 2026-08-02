package com.example.GuardBatXat.controller.auth;
import com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest;
import com.example.GuardBatXat.dto.request.rescue.ChatRequest;

import com.example.GuardBatXat.dto.request.rescue.SosRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.service.SosService;
import jakarta.validation.Valid; // Thêm import này
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sos")
@RequiredArgsConstructor
public class AuthSosController {

    private final SosService sosService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> sendSosAlert(@RequestBody @Valid SosRequest requestDto) {
        String identifier = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Integer id = sosService.processSosRequest(requestDto, identifier);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", id);
        data.put("message", "Tín hiệu SOS đã được phát đi. Đội cứu hộ đang xác định vị trí của bạn!");

        ApiResponse<java.util.Map<String, Object>> response = new ApiResponse<>(
                200,
                "Success",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/live-location")
    public ResponseEntity<ApiResponse<String>> updateLiveLocation(@RequestBody @Valid com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest requestDto) {
        sosService.updateLiveLocation(requestDto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", "Cập nhật vị trí thành công"));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> sendChat(@RequestBody @Valid com.example.GuardBatXat.dto.request.rescue.ChatRequest requestDto) {
        sosService.sendEmergencyChat(requestDto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", "Đã gửi tin nhắn"));
    }
}