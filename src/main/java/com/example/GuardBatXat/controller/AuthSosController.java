package com.example.GuardBatXat.controller;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.dto.response.ApiResponse;
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
    // Thêm @Valid vào đây
    public ResponseEntity<ApiResponse<String>> sendSosAlert(@RequestBody @Valid SosRequest requestDto) {

        sosService.processSosRequest(requestDto);

        ApiResponse<String> response = new ApiResponse<>(
                200,
                "Success",
                "Tín hiệu SOS đã được phát đi. Đội cứu hộ đang xác định vị trí của bạn!"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/live-location")
    public ResponseEntity<ApiResponse<String>> updateLiveLocation(@RequestBody @Valid com.example.GuardBatXat.dto.request.LiveLocationRequest requestDto) {
        sosService.updateLiveLocation(requestDto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", "Cập nhật vị trí thành công"));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> sendChat(@RequestBody @Valid com.example.GuardBatXat.dto.request.ChatRequest requestDto) {
        sosService.sendEmergencyChat(requestDto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", "Đã gửi tin nhắn"));
    }
}