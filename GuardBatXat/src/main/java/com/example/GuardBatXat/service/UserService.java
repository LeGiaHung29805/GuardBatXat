package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.UserCreationRequest;
import com.example.GuardBatXat.dto.request.UserUpdateRequest;
import com.example.GuardBatXat.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse createUser(UserCreationRequest request);
    void toggleUserStatus(Integer userId);
    void deleteUser(Integer userId);
    UserResponse updateUser(Integer userId, UserUpdateRequest request);
}