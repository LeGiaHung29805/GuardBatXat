package com.example.GuardBatXat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    //Thông tin tài khoản
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String roleName;
    private Boolean isActive;

    //Thông tin Hồ sơ sinh tồn
    private Integer totalMembers;
    private Integer elderlyCount;
    private Integer childrenCount;
    private Integer disabledCount;

    private String emergencyContactName;
    private String emergencyContactPhone;

    private String medicalNotes;
    private String specialAssets;
}