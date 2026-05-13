package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SosUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosUpdateLogRepository extends JpaRepository<SosUpdateLog, Integer> {
    List<SosUpdateLog> findBySosRequestIdOrderByCreatedAtDesc(Integer sosRequestId);
}
