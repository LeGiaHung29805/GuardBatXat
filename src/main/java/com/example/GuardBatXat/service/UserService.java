package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.UserProfile;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.dto.request.auth.UserCreationRequest;
import com.example.GuardBatXat.dto.request.auth.UserProfileRequest;
import com.example.GuardBatXat.dto.request.auth.UserUpdateRequest;
import com.example.GuardBatXat.dto.request.admin.AdminUserCreateRequest;
import com.example.GuardBatXat.dto.request.admin.AdminUserUpdateRequest;
import com.example.GuardBatXat.dto.response.auth.UserProfileResponse;
import com.example.GuardBatXat.dto.response.auth.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse registerCitizen(UserCreationRequest request);
    UserResponse createUser(UserCreationRequest request);
    UserResponse createAdminUser(AdminUserCreateRequest request);
    void toggleUserStatus(Integer userId);
    void deleteUser(Integer userId);
    UserResponse updateUser(Integer userId, UserUpdateRequest request);
    UserResponse updateAdminUser(Integer userId, AdminUserUpdateRequest request);
    UserResponse getMyProfile(String identifier);
    UserProfileResponse getMySurvivalProfile(String identifier);
    UserProfileResponse updateMySurvivalProfile(String identifier, UserProfileRequest request);
}
