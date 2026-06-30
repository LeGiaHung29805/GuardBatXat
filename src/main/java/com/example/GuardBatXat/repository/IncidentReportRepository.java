package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.IncidentReport;
import com.example.GuardBatXat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Integer> {
    List<IncidentReport> findByStatus(String status);
    List<IncidentReport> findByReporterOrderByCreatedAtDesc(User reporter);
    long countByStatus(String status);
}
