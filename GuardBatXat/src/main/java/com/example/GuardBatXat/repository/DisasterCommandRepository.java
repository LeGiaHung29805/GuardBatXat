package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.entity.FloodSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface DisasterCommandRepository extends JpaRepository<FloodSimulation, Integer> {

    // 1. Lấy Heatmap Ngập lụt (Trả về GeoJSON để map Frontend dễ đọc)
    @Query(value = "SELECT building_id, flood_depth, risk_status, ST_AsGeoJSON(geom) as geojson " +
            "FROM simulate_flood_risk(:floodLevel)", nativeQuery = true)
    List<Map<String, Object>> getFloodHeatmap(@Param("floodLevel") BigDecimal floodLevel);

    // 2. Lấy Heatmap Sạt lở
    @Query(value = "SELECT building_id, combined_score, risk_severity, ST_AsGeoJSON(geom) as geojson " +
            "FROM get_combined_landslide_risk()", nativeQuery = true)
    List<Map<String, Object>> getLandslideHeatmap();

    // 3. Kích hoạt kịch bản: Insert hàng loạt kết quả từ Function vào bảng simulation
    @Modifying
    @Query(value = "INSERT INTO batxat_flood_simulation (simulation_id, input_level, building_id, depth_impact, risk_status) " +
            "SELECT CAST(:simId AS UUID), :floodLevel, building_id, flood_depth, risk_status " +
            "FROM simulate_flood_risk(:floodLevel)", nativeQuery = true)
    void executeEvacuationScenario(@Param("simId") String simId, @Param("floodLevel") BigDecimal floodLevel);

    // 4. Thống kê hậu thiên tai theo kịch bản
    @Query(value = "SELECT risk_status, COUNT(building_id) as total_buildings " +
            "FROM batxat_flood_simulation WHERE simulation_id = CAST(:simId AS UUID) " +
            "GROUP BY risk_status", nativeQuery = true)
    List<Map<String, Object>> getDamageStatistics(@Param("simId") String simId);

    // 5. Thống kê thiệt hại Nhà cửa, Dân số, Diện tích theo kịch bản mực nước
    @Query(value = "SELECT " +
            "COUNT(id) as total_flooded_buildings, " +
            "COALESCE(SUM(estimated_pop), 0) as total_affected_people, " +
            "COALESCE(SUM(area_in_meters), 0) as total_flooded_area " +
            "FROM batxat_buildings WHERE elevation_z < :floodLevel", nativeQuery = true)
    Map<String, Object> getBuildingFloodStats(@Param("floodLevel") BigDecimal floodLevel);

    // 6. Thống kê Tuyến đường bị chặn (Ngập)
    @Query(value = "SELECT " +
            "COUNT(*) as total_blocked_roads, " +
            "COALESCE(SUM(length_m), 0) as total_blocked_length_m " +
            "FROM batxat_road_edges WHERE avg_elevation < :floodLevel", nativeQuery = true)
    Map<String, Object> getRoadFloodStats(@Param("floodLevel") BigDecimal floodLevel);

    // Lấy thống kê Nhà, Người và Diện tích
    @Query(value = "SELECT " +
            "COUNT(id) as nha_bi_ngap, " +
            "COALESCE(SUM(max_capacity), 0) as nguoi_can_so_tan, " +
            "COALESCE(SUM(area_in_meters), 0) as dien_tich_ngap_m2 " +
            "FROM batxat_buildings WHERE elevation_z < :floodLevel", nativeQuery = true)
    Map<String, Object> getBuildingStatsForDashboard(@Param("floodLevel") BigDecimal floodLevel);

    // Lấy thống kê số tuyến đường bị chia cắt
    @Query(value = "SELECT COUNT(*) as duong_bi_chan " +
            "FROM batxat_road_edges WHERE avg_elevation < :floodLevel", nativeQuery = true)
    Map<String, Object> getRoadStatsForDashboard(@Param("floodLevel") BigDecimal floodLevel);

    // 1. Biểu đồ Mức độ Nghiêm trọng (Tính trực tiếp từ độ ngập và khoảng cách bờ sông)
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN (:floodLevel - elevation_z) > 2 THEN 'Cực kỳ nghiêm trọng' " +
            "  WHEN (:floodLevel - elevation_z) > 0 THEN 'Nghiêm trọng' " +
            "  WHEN dist_to_water < 50 THEN 'Trung bình' " +
            "  ELSE 'Nhẹ' END as muc_do, " +
            "COUNT(id) as so_luong " +
            "FROM batxat_buildings " +
            "WHERE elevation_z < :floodLevel OR dist_to_water < 50 " +
            "GROUP BY muc_do", nativeQuery = true)
    List<Map<String, Object>> getSeverityStats(@Param("floodLevel") BigDecimal floodLevel);

    // 2. Top 5 Khu vực (Sử dụng AI phân cụm không gian ST_ClusterDBSCAN của PostGIS)
    @Query(value = "SELECT " +
            "'Cụm dân cư số ' || cluster_id as ten_khu_vuc, " +
            "COUNT(id) as so_nha_ngap, " +
            "COALESCE(SUM(max_capacity), 0) as so_nguoi, " +
            "ST_AsGeoJSON(ST_Centroid(ST_Collect(geom))) as center_geojson " +
            "FROM ( " +
            "  SELECT id, max_capacity, geom, " +
            "  ST_ClusterDBSCAN(geom, 0.001, 1) OVER () as cluster_id " + // Bán kính gom cụm ~100m
            "  FROM batxat_buildings " +
            "  WHERE elevation_z < :floodLevel " +
            ") as clustered_data " +
            "WHERE cluster_id IS NOT NULL " +
            "GROUP BY cluster_id " +
            "ORDER BY so_nha_ngap DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getTop5AffectedAreas(@Param("floodLevel") BigDecimal floodLevel);

    // 4. Lấy dữ liệu Biểu đồ đường (Line Chart) 7 ngày gần nhất
    @Query(value = "WITH last_7_days AS ( " +
            "  SELECT forecast_date, flood_risk_pct, landslide_risk_pct " +
            "  FROM batxat_daily_risk " +
            "  ORDER BY forecast_date DESC " +
            "  LIMIT 7 " +
            ") " +
            "SELECT " +
            "  TO_CHAR(forecast_date, 'DD/MM') as ngay, " +
            "  flood_risk_pct as rui_ro_ngap, " +
            "  landslide_risk_pct as rui_ro_sat_lo " +
            "FROM last_7_days " +
            "ORDER BY forecast_date ASC", nativeQuery = true)
    List<Map<String, Object>> getDamageTrendOverTime();

    // Lấy thống kê Thiệt hại theo loại (Nhà ngập, Đường chặn, Sạt lở, Hạ tầng)
    @Query(value = "SELECT 'Nhà ngập' as loai_thiet_hai, COUNT(id) as so_luong FROM batxat_buildings WHERE elevation_z < :floodLevel " +
            "UNION ALL " +
            "SELECT 'Đường chặn' as loai_thiet_hai, COUNT(*) as so_luong FROM batxat_road_edges WHERE avg_elevation < :floodLevel " +
            "UNION ALL " +
            "SELECT 'Sạt lở' as loai_thiet_hai, COUNT(*) as so_luong FROM batxat_road_edges WHERE avg_slope > 30 " +
            "UNION ALL " +
            "SELECT 'Hạ tầng hư hỏng' as loai_thiet_hai, COUNT(id) as so_luong FROM batxat_buildings WHERE elevation_z < :floodLevel AND building_type = 'Public Shelter'",
            nativeQuery = true)
    List<Map<String, Object>> getDamageByTypeStats(@Param("floodLevel") BigDecimal floodLevel);

    // 1. Lưu cảnh báo (Không cần cột target_area)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO batxat_notifications (title, content, alert_level, created_at) " +
            "VALUES (:title, :content, :level, CURRENT_TIMESTAMP)", nativeQuery = true)
    void saveAlertHistory(@Param("title") String title,
                          @Param("content") String content,
                          @Param("level") String level);

    // 2. Lấy danh sách lịch sử gốc
    @Query(value = "SELECT notify_id, title, content, alert_level, " +
            "TO_CHAR(created_at, 'HH24:MI - DD/MM/YYYY') as thoi_gian " +
            "FROM batxat_notifications " +
            "ORDER BY created_at DESC", nativeQuery = true)
    List<Map<String, Object>> getRawAlertHistory();

    // ========================================================
    // HEATMAP DÀNH CHO CHỈ HUY (KÈM DỮ LIỆU CHI TIẾT ĐỂ HIỂN THỊ POPUP)
    // ========================================================

    // 1. Heatmap Ngập lụt (Chỉ huy) - Kèm số người, diện tích, loại nhà
    @Query(value = "SELECT " +
            "id, " +
            "ST_AsGeoJSON(ST_Centroid(geom)) as geojson, " + // Giữ nguyên geom vì bảng buildings dùng geom
            "elevation_z as cao_do, " +
            "COALESCE(max_capacity, 0) as so_nguoi, " +
            "building_type as loai_nha, " +
            "area_in_meters as dien_tich " +
            "FROM batxat_buildings " +
            "WHERE elevation_z < :floodLevel", nativeQuery = true)
    List<Map<String, Object>> getCommanderFloodHeatmap(@Param("floodLevel") BigDecimal floodLevel);

    // 2. Heatmap Sạt lở (Chỉ huy) - Kèm độ dốc, sức chứa đường, có phải là cầu không
    @Query(value = "SELECT " +
            "key as id, " +
            "ST_AsGeoJSON(ST_Centroid(geometry)) as geojson, " + // Đã sửa thành geometry cho bảng road_edges
            "avg_slope as do_doc, " +
            "road_capacity as luu_luong_xe, " +
            "is_bridge as la_cau " +
            "FROM batxat_road_edges " +
            "WHERE avg_slope > 30", nativeQuery = true)
    List<Map<String, Object>> getCommanderLandslideHeatmap();

    // Lấy dữ liệu không gian cho Bản đồ (Hỗ trợ cả Heatmap và Point)
    @Query(value = "SELECT id, " +
            "ST_Y(ST_Centroid(geom)) as lat, " +
            "ST_X(ST_Centroid(geom)) as lng, " +
            "COALESCE(landslide_prob, 0) as weight_prob, " +
            "risk_status " +
            "FROM batxat_buildings " +
            "WHERE landslide_prob > 0.1 OR risk_status LIKE 'Nguy cơ%'", nativeQuery = true)
    List<Map<String, Object>> getMapSpatialData();




}