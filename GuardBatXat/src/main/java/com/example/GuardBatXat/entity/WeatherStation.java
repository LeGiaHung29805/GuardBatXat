package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "batxat_weather_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherStation {
    @Id
    @Column(name = "station_code", length = 20)
    private String stationCode;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    private Double elevation;
}