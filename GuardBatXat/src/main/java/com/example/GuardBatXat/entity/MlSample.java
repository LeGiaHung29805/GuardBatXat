package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "batxat_ml_samples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MlSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double elevation;
    private Double slope;

    @Column(name = "dist_to_water")
    private Double distToWater;

    @Column(name = "landslide_label")
    private Integer landslideLabel;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;
}