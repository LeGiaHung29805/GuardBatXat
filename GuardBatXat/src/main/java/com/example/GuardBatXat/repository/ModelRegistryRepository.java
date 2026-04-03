package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.ModelRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRegistryRepository extends JpaRepository<ModelRegistry, Integer> {

    // Tìm mô hình đang hoạt động (Active) cho loại thiên tai cụ thể (FLOOD hoặc LANDSLIDE)
    Optional<ModelRegistry> findByModelTargetAndIsActiveTrue(String modelTarget);

    // Dùng khi Admin kích hoạt 1 model mới -> Phải tắt hết các model cùng loại thiên tai trước đó
    @Modifying
    @Query("UPDATE ModelRegistry m SET m.isActive = false WHERE m.modelTarget = :target")
    void deactivateAllModelsByTarget(@Param("target") String target);
}