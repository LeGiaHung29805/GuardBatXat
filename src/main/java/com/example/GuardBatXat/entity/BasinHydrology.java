package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "batxat_basin_hydrology")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasinHydrology {
    @Id
    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "basin_precip")
    private Double basinPrecip;

    @Column(name = "estimated_water_level")
    private Double estimatedWaterLevel;

    @Column(name = "effective_rainfall_mm")
    private Double effectiveRainfallMm;

    @Column(name = "runoff_volume_m3")
    private Double runoffVolumeM3;

    @Column(name = "runoff_mean_discharge_m3s")
    private Double runoffMeanDischargeM3s;

    @Column(name = "adjusted_curve_number")
    private Double adjustedCurveNumber;

    @Column(name = "hydrology_method")
    private String hydrologyMethod;

    @Column(name = "is_event")
    private Boolean isEvent;

    @Column(name = "real_impact_score")
    private Integer realImpactScore;
}
