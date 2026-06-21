package com.example.GuardBatXat.repository;
import com.example.GuardBatXat.entity.Building;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CommanderAnalysisRepository {

    private final JdbcTemplate jdbcTemplate;

    // 1. Lấy thiệt hại theo loại
    public Integer countFloodedBuildings(Double level) {
        String sql = "SELECT COUNT(id) FROM batxat_buildings WHERE elevation_z < ?::numeric AND building_type = 'Private House'";
        return jdbcTemplate.queryForObject(sql, Integer.class, level);
    }

    public Integer countFloodedRoads(Double level) {
        // Gộp cả 2 nguyên nhân (Ngập hoặc Sạt lở)
        // VÀ Lọc không gian (Spatial Filter): Chỉ đếm những tuyến đường nằm trong ranh giới Bát Xát mới (bảng batxatmoi)
        String sql = """
            SELECT COUNT(*) 
            FROM batxat_road_edges e
            LEFT JOIN batxat_daily_road_risk r ON e.u = r.u_node AND e.v = r.v_node AND e.key = r.key AND r.forecast_date = CURRENT_DATE
            WHERE ((e.avg_elevation > 0 AND e.avg_elevation <= ?::numeric) OR r.landslide_prob > 0)
              AND EXISTS (
                  SELECT 1 FROM batxatmoi bx 
                  WHERE ST_Intersects(e.geometry, ST_SetSRID(bx.geom, 4326))
              )
            """;
        return jdbcTemplate.queryForObject(sql, Integer.class, level);
    }

    public Integer countLandslideRiskBuildings() {
        String sql = "SELECT COUNT(*) FROM get_combined_landslide_risk() WHERE combined_score >= 0.55";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Integer countDamagedInfrastructure(Double level) {
        String sql = "SELECT COUNT(id) FROM batxat_buildings WHERE elevation_z < ?::numeric AND building_type != 'Private House'";
        return jdbcTemplate.queryForObject(sql, Integer.class, level);
    }

    // 2. Lấy biểu đồ mức độ nghiêm trọng
    public List<Map<String, Object>> getSeverityChart(Double level) {
        String sql = """
            SELECT
                CASE 
                    WHEN ?::numeric - b.elevation_z > 2 OR l.risk_severity = 'Rất Cao (Nguy cấp)' THEN 'Cực kỳ nghiêm trọng'
                    WHEN ?::numeric - b.elevation_z > 0 OR l.risk_severity = 'Cao' THEN 'Nghiêm trọng'
                    WHEN l.risk_severity = 'Trung Bình' THEN 'Trung bình'
                    ELSE 'Nhẹ'
                END AS muc_do,
                COUNT(*) AS so_luong
            FROM batxat_buildings b
            LEFT JOIN get_combined_landslide_risk() l ON b.id = l.building_id
            GROUP BY 1
            """;
        return jdbcTemplate.queryForList(sql, level, level);
    }

    // 3. Lấy xu hướng thiệt hại (7 ngày)
    public List<Map<String, Object>> getDamageTrend() {
        String sql = """
            WITH last_7_days AS (
                SELECT 
                    TO_CHAR(forecast_date, 'DD/MM') AS ngay,
                    flood_risk_pct AS rui_ro_ngap,
                    landslide_risk_pct AS rui_ro_sat_lo,
                    forecast_date
                FROM batxat_daily_risk
                ORDER BY forecast_date DESC
                LIMIT 7
            )
            SELECT ngay, rui_ro_ngap, rui_ro_sat_lo
            FROM last_7_days
            ORDER BY forecast_date ASC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // 4. Lấy Top 5 khu vực thiệt hại nặng nhất
    public List<Map<String, Object>> getTopAreas(Double level) {
        String sql = """
            SELECT 
                bx.name_3 AS ten_khu_vuc,
                COUNT(b.id) AS so_nha_ngap,
                COALESCE(SUM(b.estimated_pop), 0) AS so_nguoi
            FROM batxat_buildings b
            JOIN batxatmoi bx ON ST_Intersects(ST_SetSRID(b.geom, 4326), ST_SetSRID(bx.geom, 4326))
            WHERE b.elevation_z < ?::numeric
            GROUP BY bx.name_3
            ORDER BY so_nha_ngap DESC
            LIMIT 5
            """;
        return jdbcTemplate.queryForList(sql, level);
    }

    // 5. Xếp hạng TẤT CẢ các xã cũ của Bát Xát theo nhiều tiêu chí
    //    (Nhà ngập, Số người, Diện tích ngập, Đường bị chặn)
    public List<Map<String, Object>> getCommuneRanking(Double level) {
        String sql = """
            WITH building_stats AS (
                SELECT bx.name_3 AS commune,
                       COUNT(b.id) AS so_nha_ngap,
                       COALESCE(SUM(b.estimated_pop), 0) AS so_nguoi,
                       COALESCE(SUM(b.area_in_meters), 0) AS dien_tich
                FROM batxat_buildings b
                JOIN batxatmoi bx ON ST_Intersects(ST_SetSRID(b.geom, 4326), ST_SetSRID(bx.geom, 4326))
                WHERE b.elevation_z < ?::numeric
                GROUP BY bx.name_3
            ),
            road_stats AS (
                SELECT bx.name_3 AS commune,
                       COUNT(*) AS so_duong_chan
                FROM batxat_road_edges e
                JOIN batxatmoi bx ON ST_Intersects(e.geometry, ST_SetSRID(bx.geom, 4326))
                LEFT JOIN batxat_daily_road_risk r 
                       ON e.u = r.u_node AND e.v = r.v_node AND e.key = r.key 
                      AND r.forecast_date = CURRENT_DATE
                WHERE (e.avg_elevation > 0 AND e.avg_elevation <= ?::numeric) 
                   OR r.landslide_prob > 0
                GROUP BY bx.name_3
            )
            SELECT COALESCE(b.commune, r.commune) AS ten_khu_vuc,
                   COALESCE(b.so_nha_ngap, 0) AS so_nha_ngap,
                   COALESCE(b.so_nguoi, 0) AS so_nguoi,
                   ROUND(COALESCE(b.dien_tich, 0))::int AS dien_tich,
                   COALESCE(r.so_duong_chan, 0) AS so_duong_chan
            FROM building_stats b
            FULL OUTER JOIN road_stats r ON b.commune = r.commune
            WHERE COALESCE(b.commune, r.commune) IS NOT NULL
            ORDER BY so_nha_ngap DESC, so_duong_chan DESC
            """;
        return jdbcTemplate.queryForList(sql, level, level);
    }

    // 6. Dự báo diễn biến Mực nước & Lượng mưa lưu vực (14 mốc thời gian gần nhất)
    public List<Map<String, Object>> getWaterLevelForecast() {
        String sql = """
            SELECT ngay, luong_mua, muc_nuoc
            FROM (
                SELECT record_date,
                       TO_CHAR(record_date, 'DD/MM') AS ngay,
                       COALESCE(basin_precip, 0) AS luong_mua,
                       COALESCE(estimated_water_level, 0) AS muc_nuoc
                FROM batxat_basin_hydrology
                ORDER BY record_date DESC
                LIMIT 14
            ) t
            ORDER BY t.record_date ASC
            """;
        return jdbcTemplate.queryForList(sql);
    }
}
