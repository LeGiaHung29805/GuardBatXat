package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.Building;
import com.example.GuardBatXat.repository.projection.BuildingLocationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query("""
        SELECT b FROM Building b
        WHERE :search = ''
           OR CAST(b.id AS string) LIKE CONCAT('%', :search, '%')
           OR LOWER(COALESCE(b.buildingType, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<Building> findAdminPage(@Param("search") String search, Pageable pageable);

    @Query(value = """
        SELECT b.id AS "buildingId",
               CAST(ST_Y(ST_PointOnSurface(b.geom)) AS double precision) AS latitude,
               CAST(ST_X(ST_PointOnSurface(b.geom)) AS double precision) AS longitude
        FROM batxat_buildings b
        WHERE b.geom IS NOT NULL
          AND NOT ST_IsEmpty(b.geom)
        ORDER BY b.geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
        LIMIT 1
        """, nativeQuery = true)
    Optional<BuildingLocationView> findNearestLocation(
            @Param("lng") Double lng,
            @Param("lat") Double lat
    );

    @Query(value = """
        SELECT b.id AS "buildingId",
               CAST(ST_Y(ST_PointOnSurface(b.geom)) AS double precision) AS latitude,
               CAST(ST_X(ST_PointOnSurface(b.geom)) AS double precision) AS longitude
        FROM batxat_buildings b
        WHERE b.id = :id
          AND b.geom IS NOT NULL
        """, nativeQuery = true)
    Optional<BuildingLocationView> findLocationById(@Param("id") Long id);

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
