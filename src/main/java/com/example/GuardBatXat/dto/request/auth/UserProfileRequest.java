package com.example.GuardBatXat.dto.request.auth;
import com.example.GuardBatXat.entity.UserProfile;
import com.example.GuardBatXat.entity.User;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String fullName;
    private String email;
    private String phoneNumber;

    private Integer totalMembers;
    private Integer elderlyCount;
    private Integer childrenCount;
    private Integer disabledCount;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalNotes;
    private String specialAssets;
}