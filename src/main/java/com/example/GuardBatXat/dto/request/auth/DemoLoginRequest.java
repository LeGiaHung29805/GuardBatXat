package com.example.GuardBatXat.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemoLoginRequest {

    @NotBlank(message = "Mã đăng nhập không được để trống")
    @Size(min = 40, max = 200, message = "Mã đăng nhập không hợp lệ")
    private String token;
}
