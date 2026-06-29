package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.commander.HeatmapProjection;
import com.example.GuardBatXat.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HeatmapRepository extends JpaRepository<Building, Long> {

    // Lấy dữ liệu Sạt Lở từ AI kết hợp Ngập lụt (Bản đồ kép)
    @Query(value = "SELECT ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "combined_score as weight, " +
            "risk_severity as severity " +
            "FROM get_combined_landslide_risk() " +
            "WHERE combined_score > 0 " +
            "UNION ALL " +
            "SELECT ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "LEAST(flood_depth / 2.0, 1.0) as weight, " +
            "risk_status as severity " +
            "FROM simulate_flood_risk(CAST((SELECT active_flood_level FROM batxat_system_state ORDER BY updated_at DESC LIMIT 1) AS numeric)) " +
            "WHERE flood_depth > 0",
            nativeQuery = true)
    List<HeatmapProjection> getLandslideHeatmap();

    @Query(value = "SELECT ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "LEAST(flood_depth / 2.0, 1.0) as weight, " +
            "risk_status as severity " +
            "FROM simulate_flood_risk(CAST(:waterLevel AS numeric)) " +
            "WHERE flood_depth > 0",
            nativeQuery = true)
    List<HeatmapProjection> getFloodHeatmap(@Param("waterLevel") Double waterLevel);
}