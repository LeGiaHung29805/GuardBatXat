package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "batxat_safe_havens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafeHaven {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255)
    private String name;

    @Column(name = "haven_type", length = 50)
    private String havenType;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    @Column(insertable = false, updatable = false)
    private Double longitude;

    @Column(insertable = false, updatable = false)
    private Double latitude;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_occupancy")
    private Integer currentOccupancy;

    @Column(name = "is_accessible")
    private Boolean isAccessible;
}