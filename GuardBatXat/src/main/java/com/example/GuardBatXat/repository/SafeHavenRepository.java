package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.SafeHaven;
import com.example.GuardBatXat.dto.response.SafeHavenProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafeHavenRepository extends JpaRepository<SafeHaven, Integer> {

    @Query(value = """
        SELECT 
            id, name, haven_type as havenType, longitude, latitude, 
            max_capacity as maxCapacity, current_occupancy as currentOccupancy,
            ST_Distance(geom::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) as distance
        FROM batxat_safe_havens
        WHERE is_accessible = true 
          AND current_occupancy < max_capacity
        ORDER BY geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
        LIMIT :limit
        """, nativeQuery = true)
    List<SafeHavenProjection> findNearestAvailableHavens(
            @Param("lng") Double lng,
            @Param("lat") Double lat,
            @Param("limit") int limit
    );
}