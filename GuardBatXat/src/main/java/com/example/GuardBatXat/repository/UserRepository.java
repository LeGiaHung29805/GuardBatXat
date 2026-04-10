package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber); // Quan trọng để check trùng SĐT

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByIdentifier(@Param("id") String identifier);
}