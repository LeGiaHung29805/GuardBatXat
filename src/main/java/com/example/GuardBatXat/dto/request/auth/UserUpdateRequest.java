package com.example.GuardBatXat.dto.request.auth;
import com.example.GuardBatXat.entity.User;

import lombok.Data;

@Data
public class UserUpdateRequest {
    // Không bắt buộc nhập, nếu Admin điền thì mới đổi thông tin đó
    private String password;
    private String fullName;
    private String phoneNumber;
    private String roleName;
    private String assignedStation;
}