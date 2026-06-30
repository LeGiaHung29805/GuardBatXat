package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE n.alertLevel = :alertLevel ORDER BY n.createdAt DESC")
    List<Notification> findByAlertLevelOrderByCreatedAtDesc(String alertLevel);

    @Query("SELECT n FROM Notification n WHERE n.targetUser IS NULL " +
           "AND (n.alertLevel IS NULL OR (n.alertLevel != 'Cứu hộ' AND n.alertLevel NOT LIKE 'RESCUE_LOG_%')) " +
           "AND (n.title IS NULL OR n.title NOT LIKE 'Field Update%') " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findTop20GeneralNotifications();

    @Query("SELECT n FROM Notification n WHERE (n.targetUser = :user) " +
           "OR (n.targetUser IS NULL " +
           "  AND (n.alertLevel IS NULL OR (n.alertLevel != 'Cứu hộ' AND n.alertLevel NOT LIKE 'RESCUE_LOG_%')) " +
           "  AND (n.title IS NULL OR n.title NOT LIKE 'Field Update%')) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findTop20ForUserOrderByCreatedAtDesc(@Param("user") User user);
}
