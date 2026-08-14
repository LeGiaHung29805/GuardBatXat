package com.example.GuardBatXat.config;
import com.example.GuardBatXat.dto.request.rescue.SosRequest;

import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.RoleRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.repository.SosRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "batxat.demo.seed-enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SosRequestRepository sosRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${batxat.demo.citizen-password}")
    private String citizenPassword;
    @Value("${batxat.demo.rescue-password}")
    private String rescuePassword;
    @Value("${batxat.demo.commander-password}")
    private String commanderPassword;
    @Value("${batxat.demo.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (citizenPassword.isBlank() || rescuePassword.isBlank()
                || commanderPassword.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Demo seeding requires all BATXAT_DEMO_*_PASSWORD values"
            );
        }
        // 1. Đảm bảo 4 role tồn tại trong DB
        String[] roleNames = {"CITIZEN", "RESCUE_TEAM", "COMMANDER", "ADMIN"};
        for (String roleName : roleNames) {
            if (!roleRepository.findByRoleName(roleName).isPresent()) {
                Role r = new Role();
                r.setRoleName(roleName);
                roleRepository.save(r);
                System.out.println("[GuardBatXat] Đã tự động tạo thêm Role: " + roleName);
            }
        }

        // 2. Tạo 4 tài khoản mẫu cho 4 role tương ứng
        // CITIZEN
        if (!userRepository.existsByUsername("citizen@batxat.local")) {
            Role r = roleRepository.findByRoleName("CITIZEN").get();
            User u = new User();
            u.setUsername("citizen@batxat.local");
            u.setEmail("citizen@batxat.local");
            u.setPasswordHash(passwordEncoder.encode(citizenPassword));
            u.setFullName("Người Dân Bát Xát");
            u.setIsActive(true);
            u.setRole(r);
            userRepository.save(u);
            System.out.println("[GuardBatXat] Đã tự động tạo tài khoản: citizen@batxat.local");
        }

        // RESCUE
        if (!userRepository.existsByUsername("rescue@batxat.local")) {
            Role r = roleRepository.findByRoleName("RESCUE_TEAM").get();
            User u = new User();
            u.setUsername("rescue@batxat.local");
            u.setEmail("rescue@batxat.local");
            u.setPasswordHash(passwordEncoder.encode(rescuePassword));
            u.setFullName("Đội Cứu Hộ Bát Xát");
            u.setIsActive(true);
            u.setRole(r);
            userRepository.save(u);
            System.out.println("[GuardBatXat] Đã tự động tạo tài khoản: rescue@batxat.local");
        }

        // COMMANDER
        if (!userRepository.existsByUsername("commander@batxat.local")) {
            Role r = roleRepository.findByRoleName("COMMANDER").get();
            User u = new User();
            u.setUsername("commander@batxat.local");
            u.setEmail("commander@batxat.local");
            u.setPasswordHash(passwordEncoder.encode(commanderPassword));
            u.setFullName("Chỉ Huy Trưởng Bát Xát");
            u.setIsActive(true);
            u.setRole(r);
            userRepository.save(u);
            System.out.println("[GuardBatXat] Đã tự động tạo tài khoản: commander@batxat.local");
        }

        // ADMIN
        if (!userRepository.existsByUsername("admin@batxat.local")) {
            Role r = roleRepository.findByRoleName("ADMIN").get();
            User u = new User();
            u.setUsername("admin@batxat.local");
            u.setEmail("admin@batxat.local");
            u.setPasswordHash(passwordEncoder.encode(adminPassword));
            u.setFullName("Quản Trị Viên Hệ Thống");
            u.setIsActive(true);
            u.setRole(r);
            userRepository.save(u);
            System.out.println("[GuardBatXat] Đã tự động tạo tài khoản: admin@batxat.local");
        }

        // Seed SOS Requests for Rescue Team to see
        if (sosRequestRepository.count() == 0) {
            sosRequestRepository.insertSosRequestNative("0909123456", "3 người mắc kẹt trên mái nhà, nước dâng nhanh!", 22.62, 103.72, "Người dân 1", 3, 0, 0, null);
            sosRequestRepository.insertSosRequestNative("0909234567", "Nước ngập 1.5m, cần di dời 2 người già", 22.605, 103.71, "Người dân 2", 2, 2, 0, null);
            sosRequestRepository.insertSosRequestNative("0909345678", "Đất sạt lở chia cắt đường, cần hỗ trợ y tế", 22.63, 103.725, "Người dân 3", 1, 0, 0, null);
            System.out.println("[GuardBatXat] Đã tự động bơm 3 tín hiệu SOS mồi vào Database!");
        }
    }
}
