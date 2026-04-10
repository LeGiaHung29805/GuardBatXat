package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {

    @NotBlank(message = "Vui lòng nhập Email hoặc Số điện thoại")
    private String emailOrPhone; // Trường dùng chung để người dân nhập

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
    private String password;

    private String fullName;

    private String roleName;

    private String assignedStation;
}