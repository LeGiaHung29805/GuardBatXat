package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SystemState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemStateRepository extends JpaRepository<SystemState, Long> {
    SystemState findFirstByOrderByIdDesc();
}