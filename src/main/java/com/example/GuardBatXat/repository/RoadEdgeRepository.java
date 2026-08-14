package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.admin.RoadEdgeListDto;
import com.example.GuardBatXat.entity.RoadEdge;
import com.example.GuardBatXat.entity.RoadEdgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadEdgeRepository extends JpaRepository<RoadEdge, RoadEdgeId> {
    @Modifying
    @Query(value = """
        INSERT INTO batxat_road_edges (
            u, v, key, length_m, road_capacity, is_bridge,
            community_report, avg_slope, avg_elevation,
            cost_safety, cost_speed, geometry
        )
        VALUES (
            :u, :v, :key, COALESCE(:length, 0), COALESCE(:capacity, 3), COALESCE(:isBridge, 0),
            0, 0, 0,
            COALESCE(:length, 0), COALESCE(:length, 0), ST_GeomFromText(:wkt, 4326)
        )
        """, nativeQuery = true)
    void insertRoadEdgeNative(
            @Param("u") Long u, @Param("v") Long v, @Param("key") Integer key,
            @Param("length") Double length, @Param("capacity") Integer capacity,
            @Param("isBridge") Integer isBridge, @Param("wkt") String wkt
    );
    @Query("""
        SELECT new com.example.GuardBatXat.dto.response.admin.RoadEdgeListDto(e.key, e.u, e.v, e.lengthM, e.avgSlope)
        FROM RoadEdge e
        WHERE :search = ''
           OR CAST(e.key AS string) LIKE CONCAT('%', :search, '%')
           OR CAST(e.u AS string) LIKE CONCAT('%', :search, '%')
           OR CAST(e.v AS string) LIKE CONCAT('%', :search, '%')
        """)
    Page<RoadEdgeListDto> findAdminPage(@Param("search") String search, Pageable pageable);

    @Modifying
    @Query(value = """
        UPDATE batxat_road_edges 
        SET community_report = LEAST(COALESCE(community_report, 0) + :amount, 5)
        WHERE ST_DWithin(geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :distanceDegree)
        """, nativeQuery = true)
    void incrementCommunityReportNear(
            @Param("lat") Double lat, 
            @Param("lng") Double lng, 
            @Param("distanceDegree") Double distanceDegree,
            @Param("amount") Integer amount
    );
}
