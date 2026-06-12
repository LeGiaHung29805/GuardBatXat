package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE n.alertLevel = :alertLevel ORDER BY n.createdAt DESC")
    List<Notification> findByAlertLevelOrderByCreatedAtDesc(String alertLevel);
}
