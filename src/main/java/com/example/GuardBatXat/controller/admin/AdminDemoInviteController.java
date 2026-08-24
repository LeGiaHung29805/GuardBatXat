package com.example.GuardBatXat.controller.admin;

import com.example.GuardBatXat.dto.request.admin.DemoInviteRequest;
import com.example.GuardBatXat.dto.response.admin.DemoInviteResponse;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.service.DemoLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/demo-invites")
@RequiredArgsConstructor
public class AdminDemoInviteController {

    private final DemoLoginService demoLoginService;

    @PostMapping
    public ResponseEntity<ApiResponse<DemoInviteResponse>> createInvite(
            @RequestBody @Valid DemoInviteRequest request
    ) {
        DemoInviteResponse invite = demoLoginService.createInvite(request);
        return ResponseEntity.ok(ApiResponse.<DemoInviteResponse>builder()
                .code(200)
                .message("Đã tạo mã QR đăng nhập một lần")
                .data(invite)
                .build());
    }
}
