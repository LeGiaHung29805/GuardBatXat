package com.example.GuardBatXat.config;

import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
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
    }
}