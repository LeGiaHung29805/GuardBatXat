package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.AhpWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AhpWeightRepository extends JpaRepository<AhpWeight, String> {
}