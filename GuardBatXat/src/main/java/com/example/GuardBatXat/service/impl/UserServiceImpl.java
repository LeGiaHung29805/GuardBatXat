package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.UserCreationRequest;
import com.example.GuardBatXat.dto.response.UserResponse;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.mapper.UserMapper;
import com.example.GuardBatXat.repository.RoleRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.GuardBatXat.dto.request.UserUpdateRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền: " + request.getRoleName()));

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setRole(role);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void toggleUserStatus(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (user.getRole() != null && "ADMIN".equals(user.getRole().getRoleName())) {
            throw new RuntimeException("Không thể khóa tài khoản Quản trị viên!");
        }

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Cập nhật từng trường nếu có dữ liệu gửi lên
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAssignedStation() != null) user.setAssignedStation(request.getAssignedStation());

        // Đổi quyền (Role)
        if (request.getRoleName() != null) {
            Role role = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền: " + request.getRoleName()));
            user.setRole(role);
        }

        // Đổi mật khẩu (nếu có điền) -> Băm lại mật khẩu mới
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }
}