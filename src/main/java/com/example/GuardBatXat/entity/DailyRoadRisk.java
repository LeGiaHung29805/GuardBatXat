package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "batxat_daily_road_risk", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"forecast_date", "u_node", "v_node", "key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRoadRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "forecast_date")
    private LocalDate forecastDate;

    @Column(name = "u_node")
    private Long uNode;

    @Column(name = "v_node")
    private Long vNode;

    private Integer key;

    @Column(name = "landslide_prob")
    private Float landslideProb;

    @Column(name = "flood_risk_pct")
    private Double floodRiskPct;

    @Column(name = "combined_risk_pct")
    private Double combinedRiskPct;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "elevation_known")
    private Boolean elevationKnown;

    @Column(name = "in_study_area")
    private Boolean inStudyArea;

    @Column(name = "flood_depth_m")
    private Double floodDepthM;

    @Column(name = "risk_source", length = 80)
    private String riskSource;

    @Column(name = "forecast_run_id")
    private UUID forecastRunId;

    @Column(name = "hydraulic_run_id")
    private UUID hydraulicRunId;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
