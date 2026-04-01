package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_daily_risk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "forecast_date", unique = true, nullable = false)
    private LocalDate forecastDate;

    @Column(name = "flood_risk_pct")
    private Double floodRiskPct;

    @Column(name = "landslide_risk_pct")
    private Double landslideRiskPct;

    @Column(name = "alert_level", length = 50)
    private String alertLevel;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}