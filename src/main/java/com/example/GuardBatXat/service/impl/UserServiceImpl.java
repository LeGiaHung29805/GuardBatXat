package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.UserCreationRequest;
import com.example.GuardBatXat.dto.request.UserProfileRequest;
import com.example.GuardBatXat.dto.response.UserProfileResponse;
import com.example.GuardBatXat.dto.response.UserResponse;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.entity.UserProfile;
import com.example.GuardBatXat.mapper.UserMapper;
import com.example.GuardBatXat.mapper.UserProfileMapper;
import com.example.GuardBatXat.repository.RoleRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.GuardBatXat.dto.request.UserUpdateRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserCreationRequest request) {
        String input = request.getEmailOrPhone().trim();

        boolean isEmail = input.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        boolean isPhone = input.matches("^(0[3|5|7|8|9])+([0-9]{8})$");

        if (!isEmail && !isPhone) {
            throw new RuntimeException("Định dạng không hợp lệ. Vui lòng nhập Email hoặc Số điện thoại đúng!");
        }

        if (userRepository.existsByUsername(input)) {
            throw new RuntimeException("Tài khoản này đã tồn tại trong hệ thống!");
        }
        if (isEmail && userRepository.existsByEmail(input)) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        if (isPhone && userRepository.existsByPhoneNumber(input)) {
            throw new RuntimeException("Số điện thoại này đã được sử dụng!");
        }

        String roleToAssign = (request.getRoleName() != null) ? request.getRoleName() : "CITIZEN";
        Role role = roleRepository.findByRoleName(roleToAssign)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền: " + roleToAssign));

        User user = new User();
        user.setUsername(input);

        if (isEmail) {
            user.setEmail(input);
        } else {
            user.setPhoneNumber(input);
        }

        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setRole(role);
        user.setAssignedStation(request.getAssignedStation());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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
    @Override
    @Transactional
    @Cacheable(value = "userProfile", key = "#identifier")
    public UserResponse getMyProfile(String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu người dùng!"));
        return userMapper.toUserResponse(user);
    }
    @Override
    @Transactional
    @Cacheable(value = "survivalProfile", key = "#identifier")
    public UserProfileResponse getMySurvivalProfile(String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        return userProfileMapper.toResponse(user.getUserProfile());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userProfile", "survivalProfile", "users"}, allEntries = true)
    public UserProfileResponse updateMySurvivalProfile(String identifier, UserProfileRequest request) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. CẬP NHẬT THÔNG TIN CƠ BẢN (Bảng batxat_users)
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setUserProfile(profile);
        }

        userProfileMapper.updateEntityFromRequest(request, profile);

        userRepository.save(user);

        return userProfileMapper.toResponse(profile);
    }
}