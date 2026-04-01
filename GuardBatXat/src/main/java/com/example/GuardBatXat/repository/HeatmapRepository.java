package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.HeatmapProjection;
import com.example.GuardBatXat.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HeatmapRepository extends JpaRepository<Building, Long> {

    // Lấy dữ liệu Sạt Lở từ AI
    @Query(value = "SELECT ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "combined_score as weight, " +
            "risk_severity as severity " +
            "FROM get_combined_landslide_risk() " +
            "WHERE combined_score > 0",
            nativeQuery = true)
    List<HeatmapProjection> getLandslideHeatmap();

    //Lấy dữ liệu Ngập Lụt theo kịch bản Mực nước
    @Query(value = "SELECT ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "flood_depth as weight, " +
            "risk_status as severity " +
            "FROM simulate_flood_risk(:waterLevel) " +
            "WHERE flood_depth > 0",
            nativeQuery = true)
    List<HeatmapProjection> getFloodHeatmap(@Param("waterLevel") Double waterLevel);
}