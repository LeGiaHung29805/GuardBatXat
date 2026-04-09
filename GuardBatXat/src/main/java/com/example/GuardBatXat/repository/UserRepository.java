package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Override
@EntityGraph(attributePaths = {"role"})
List<User> findAll();
    @EntityGraph(attributePaths = {"role"})

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}