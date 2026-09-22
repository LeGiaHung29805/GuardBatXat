package com.example.GuardBatXat.repository;

import com.example.GuardBatXat.dto.response.commander.FloodStatisticDto;
import com.example.GuardBatXat.entity.FloodSimulation;
import com.example.GuardBatXat.repository.projection.FloodSimulationMapPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FloodSimulationRepository extends JpaRepository<FloodSimulation, Integer> {

    @Modifying
    @Query(value = """
        INSERT INTO batxat_flood_simulation (simulation_id, input_level, building_id, depth_impact, risk_status) 
        SELECT CAST(:simId AS uuid), :waterLevel, building_id, flood_depth, risk_status 
        FROM simulate_flood_risk(CAST(:waterLevel AS NUMERIC)) 
        WHERE risk_status != 'An toàn'
        """, nativeQuery = true)
    void executeFloodSimulationNative(
            @Param("simId") String simId,
            @Param("waterLevel") Double waterLevel
    );

    @Query(value = """
        SELECT fs.building_id AS "buildingId",
               fs.depth_impact AS "depth",
               fs.risk_status AS "status",
               CAST(ST_X(ST_PointOnSurface(b.geom)) AS double precision) AS "lng",
               CAST(ST_Y(ST_PointOnSurface(b.geom)) AS double precision) AS "lat"
        FROM batxat_flood_simulation fs
        JOIN batxat_buildings b ON b.id = fs.building_id
        WHERE fs.simulation_id = :simulationId
        ORDER BY fs.id
        """, nativeQuery = true)
    List<FloodSimulationMapPoint> findMapPointsBySimulationId(
            @Param("simulationId") UUID simulationId
    );

    @Query("SELECT DISTINCT f.inputLevel FROM FloodSimulation f ORDER BY f.inputLevel ASC")
    List<Double> findAllSimulatedLevels();

    @Query("""
    SELECT new com.example.GuardBatXat.dto.response.commander.FloodStatisticDto(
        f.riskStatus, 
        COUNT(f.id), 
        SUM(f.building.areaInMeters), 
        SUM(f.building.maxCapacity)
    )
    FROM FloodSimulation f
    WHERE f.simulationId = :simId
    GROUP BY f.riskStatus
""")
    List<FloodStatisticDto> getStatisticsBySimulationId(@Param("simId") UUID simId);
}
