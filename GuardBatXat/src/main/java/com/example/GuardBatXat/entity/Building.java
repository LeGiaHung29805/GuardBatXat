package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "batxat_buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_in_meters")
    private Double areaInMeters;

    private Float confidence;

    @Column(name = "elevation_z")
    private Double elevationZ;

    @Column(name = "dist_to_water")
    private Double distToWater;

    @Column(name = "building_type")
    private String buildingType;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "estimated_pop")
    private Integer estimatedPop;

    @Column(name = "landslide_prob")
    private Double landslideProb; // Cột này để Python cập nhật kết quả AI

    // Kiểu dữ liệu MultiPolygon từ thư viện JTS (khớp với SQL GEOMETRY)
    @Column(columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon geom;
}