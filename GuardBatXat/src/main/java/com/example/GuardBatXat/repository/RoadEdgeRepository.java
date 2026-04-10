package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.RoadEdgeListDto;
import com.example.GuardBatXat.entity.RoadEdge;
import com.example.GuardBatXat.entity.RoadEdgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadEdgeRepository extends JpaRepository<RoadEdge, RoadEdgeId> {
    @Modifying
    @Query(value = """
        INSERT INTO batxat_road_edges (u, v, key, length_m, road_capacity, is_bridge, geom)
        VALUES (:u, :v, :key, :length, :capacity, :isBridge, ST_GeomFromText(:wkt, 4326))
        """, nativeQuery = true)
    void insertRoadEdgeNative(
            @Param("u") Long u, @Param("v") Long v, @Param("key") Integer key,
            @Param("length") Double length, @Param("capacity") Integer capacity,
            @Param("isBridge") Integer isBridge, @Param("wkt") String wkt
    );
    @Query("SELECT new com.example.GuardBatXat.dto.response.RoadEdgeListDto(e.key, e.u, e.v, e.lengthM, e.avgSlope) FROM RoadEdge e")
    List<RoadEdgeListDto> findAllOptimizedForAdmin();
}