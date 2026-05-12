package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query(value = """
        INSERT INTO batxat_buildings (area_in_meters, elevation_z, building_type, max_capacity, estimated_pop, geom)
        VALUES (:area, :elevation, :type, :capacity, :pop, ST_GeomFromText(:wkt, 4326))
        """, nativeQuery = true)
    void insertBuildingNative(
            @Param("area") Double area,
            @Param("elevation") Double elevation,
            @Param("type") String type,
            @Param("capacity") Integer capacity,
            @Param("pop") Integer pop,
            @Param("wkt") String wkt
    );

    @Modifying
    @Query(value = """
        UPDATE batxat_buildings 
        SET area_in_meters = :area, building_type = :type, max_capacity = :capacity, 
            estimated_pop = :pop, geom = ST_GeomFromText(:wkt, 4326)
        WHERE id = :id
        """, nativeQuery = true)
    void updateBuildingNative(
            @Param("id") Long id,
            @Param("area") Double area,
            @Param("type") String type,
            @Param("capacity") Integer capacity,
            @Param("pop") Integer pop,
            @Param("wkt") String wkt
    );
}