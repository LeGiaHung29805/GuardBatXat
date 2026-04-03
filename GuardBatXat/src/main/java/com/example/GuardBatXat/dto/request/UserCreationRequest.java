package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, message = "Tên đăng nhập phải từ 5 ký tự trở lên")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
    private String password;

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Phải gán quyền (Role)")
    private String roleName;

    private String assignedStation;
}