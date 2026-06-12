package com.example.GuardBatXat.config;
import com.example.GuardBatXat.dto.request.rescue.SosRequest;

import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.RoleRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.repository.SosRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SosRequestRepository sosRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role citizen = new Role();
            citizen.setRoleName("CITIZEN");

            Role rescue = new Role();
            rescue.setRoleName("RESCUE_TEAM");

            Role admin = new Role();
            admin.setRoleName("ADMIN");

            roleRepository.save(citizen);
            roleRepository.save(rescue);
            roleRepository.save(admin);

            System.out.println("[GuardBatXat] Đã tự động bơm dữ liệu Role (CITIZEN, RESCUE_TEAM, ADMIN) vào Database!");
        }

        // Seed Rescue Team user
        if (!userRepository.existsByUsername("rescue_team")) {
            Optional<Role> rescueRole = roleRepository.findByRoleName("RESCUE_TEAM");
            if (rescueRole.isPresent()) {
                User rescueUser = new User();
                rescueUser.setUsername("rescue_team");
                rescueUser.setEmail("rescue.team@batxat.local");
                rescueUser.setPasswordHash(passwordEncoder.encode("Rescue@2026"));
                rescueUser.setFullName("Đội Cứu Hộ Bát Xát");
                rescueUser.setIsActive(true);
                rescueUser.setRole(rescueRole.get());
                userRepository.save(rescueUser);
                System.out.println("[GuardBatXat] Đã tự động bơm dữ liệu User: rescue_team!");
            }
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