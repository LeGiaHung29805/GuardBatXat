package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "batxat_weather_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"record_date", "station_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_code")
    private WeatherStation station;

    private Double precip;

    @Column(name = "soil_moist")
    private Double soilMoist;

    @Column(name = "precip_3d")
    private Double precip3d;

    @Column(name = "precip_5d")
    private Double precip5d;

    @Column(name = "soil_fatigue")
    private Double soilFatigue;

    @Column(name = "interaction_risk")
    private Double interactionRisk;

    @Column(name = "day_of_year")
    private Integer dayOfYear;

    @Column(name = "sin_season")
    private Double sinSeason;

    @Column(name = "cos_season")
    private Double cosSeason;
}