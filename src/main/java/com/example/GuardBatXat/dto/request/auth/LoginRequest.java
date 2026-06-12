package com.example.GuardBatXat.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Vui lòng nhập tài khoản, email hoặc số điện thoại")
    private String identifier;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}