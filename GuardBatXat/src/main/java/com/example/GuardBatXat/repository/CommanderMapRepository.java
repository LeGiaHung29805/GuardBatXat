package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.CommanderFloodProjection;
import com.example.GuardBatXat.dto.response.CommanderLandslideProjection;
import com.example.GuardBatXat.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommanderMapRepository extends JpaRepository<Building, Long> {

    @Query(value = "SELECT ST_AsGeoJSON(ST_Centroid(f.geom)) as geojson, " +
            "CAST(ST_Y(ST_Centroid(f.geom)) AS double precision) as lat, " +
            "CAST(ST_X(ST_Centroid(f.geom)) AS double precision) as lng, " +
            "CAST(b.building_type AS varchar) as loai_nha, " +
            "CAST(COALESCE(p.total_members, 0) AS integer) as so_nguoi, " +
            "CAST(f.elevation_z AS double precision) as cao_do, " +
            "CAST(b.area_in_meters AS double precision) as dien_tich, " +
            "CAST(LEAST(f.flood_depth / 2.0, 1.0) AS double precision) as muc_do " +
            "FROM simulate_flood_risk(CAST(:waterLevel AS numeric)) f " +
            "JOIN batxat_buildings b ON f.building_id = b.id " +
            "LEFT JOIN batxat_users u ON u.default_building_id = b.id " +
            "LEFT JOIN batxat_user_profiles p ON p.user_id = u.user_id " +
            "WHERE f.flood_depth > 0",
            nativeQuery = true)
    List<CommanderFloodProjection> getCommanderFloodHeatmap(@Param("waterLevel") Double waterLevel);

    @Query(value = "SELECT ST_AsGeoJSON(ST_Centroid(e.geometry)) as geojson, " +
            "CAST(e.avg_slope AS double precision) as do_doc, " +
            "CAST(e.is_bridge AS integer) as la_cau, " +
            "CAST(r.landslide_prob AS double precision) as muc_do " +
            "FROM batxat_road_edges e " +
            "JOIN batxat_daily_road_risk r ON e.u = r.u_node AND e.v = r.v_node AND e.key = r.key " +
            "WHERE r.forecast_date = CURRENT_DATE AND r.landslide_prob > 0 " +
            "AND EXISTS (SELECT 1 FROM batxatmoi bx WHERE ST_Intersects(e.geometry, ST_SetSRID(bx.geom, 4326)))",
            nativeQuery = true)
    List<CommanderLandslideProjection> getCommanderLandslideHeatmap();

}
