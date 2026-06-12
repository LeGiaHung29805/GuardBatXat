package com.example.GuardBatXat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SafetyCheckRepository {

    private final JdbcTemplate jdbcTemplate;

    // Lấy mức ngập lũ hiện hành từ hệ thống
    public Double getCurrentSystemFloodLevel() {
        try {
            Double level = jdbcTemplate.queryForObject(
                    "SELECT active_flood_level FROM batxat_system_state LIMIT 1", Double.class);
            return level != null ? level : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // TRUY VẤN CÁ NHÂN HÓA CHO TỪNG CÔNG TRÌNH
    public List<Map<String, Object>> findSafetyDataByLocation(Double lat, Double lng, BigDecimal currentFloodLevel) {
        String sql = """
            SELECT 
                b.building_type,
                b.max_capacity,
                b.estimated_pop as current_occupancy,
                b.dist_to_water,
                b.elevation_z,
                
                -- 1. DỮ LIỆU NGẬP LỤT CHI TIẾT (Lấy từ hàm mô phỏng riêng cho nhà này)
                COALESCE(sim.flood_depth, 0.0) AS flood_depth,
                COALESCE(sim.risk_status, 'An toàn') AS flood_status,
                
                -- 2. DỮ LIỆU SẠT LỞ CHI TIẾT (Lấy từ hàm tính rủi ro riêng cho nhà này)
                COALESCE(ls.ai_prob, 0.0) AS ai_landslide_prob,
                COALESCE(ls.risk_severity, 'Thấp (An toàn)') AS landslide_status,
                
                -- 3. BỐI CẢNH LŨ LỤT TỔNG QUAN (Để cảnh báo sớm nếu AI báo mưa lớn)
                COALESCE(dr.flood_risk_pct / 100.0, 0.0) AS ai_flood_prob
                
            FROM batxat_buildings b
            CROSS JOIN (SELECT ST_SetSRID(ST_MakePoint(?, ?), 4326) AS geom) AS user_loc
            
            -- Lấy bối cảnh thời tiết chung của huyện ngày hôm nay
            LEFT JOIN batxat_daily_risk dr ON dr.forecast_date = CURRENT_DATE
            
            -- Gọi hàm tính độ sâu ngập CHỈ cho tòa nhà này
            LEFT JOIN LATERAL simulate_flood_risk(?) sim ON sim.building_id = b.id
            
            -- Gọi hàm tính điểm sạt lở tổng hợp CHỈ cho tòa nhà này
            LEFT JOIN LATERAL get_combined_landslide_risk() ls ON ls.building_id = b.id
            
            -- Tìm trong bán kính ~50m xung quanh điểm GPS
            WHERE ST_DWithin(user_loc.geom, b.geom, 0.0005)
            ORDER BY ST_Distance(user_loc.geom, b.geom) ASC
            LIMIT 1
        """;

        // Lưu ý: PostGIS nhận tham số theo thứ tự Kinh độ (lng) trước, Vĩ độ (lat) sau
        return jdbcTemplate.queryForList(sql, lng, lat, currentFloodLevel);
    }
}