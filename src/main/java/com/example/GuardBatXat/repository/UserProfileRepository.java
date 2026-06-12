package com.example.GuardBatXat.repository;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
}