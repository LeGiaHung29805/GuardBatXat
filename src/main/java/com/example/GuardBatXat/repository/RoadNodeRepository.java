package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.RoadNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadNodeRepository extends JpaRepository<RoadNode, Long> {

    // Sử dụng toán tử <-> của PostGIS để tìm điểm gần nhất cực kỳ tốc độ
    @Query(value = "SELECT node_id FROM batxat_road_nodes ORDER BY geometry <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) LIMIT 1", nativeQuery = true)
    Long findNearestNode(@Param("lat") Double lat, @Param("lng") Double lng);
}