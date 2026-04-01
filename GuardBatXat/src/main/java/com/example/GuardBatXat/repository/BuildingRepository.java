package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query(value = """
        SELECT * FROM batxat_buildings b 
        ORDER BY b.geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) 
        LIMIT 1
        """, nativeQuery = true)
    Optional<Building> findNearestBuilding(@Param("lng") Double lng, @Param("lat") Double lat);
}