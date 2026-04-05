package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "batxat_road_edges")
@IdClass(RoadEdgeId.class) // Khai báo sử dụng khóa chính kết hợp
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoadEdge {

    @Id
    private Long u;

    @Id
    private Long v;

    @Id
    private Integer key;

    @Column(name = "length_m")
    private Double lengthM;

    @Column(name = "avg_slope")
    private Double avgSlope;

    @Column(name = "avg_elevation")
    private Double avgElevation;

    @Column(name = "road_capacity")
    private Integer roadCapacity;

    @Column(name = "is_bridge")
    private Integer isBridge;

    @Column(name = "community_report")
    private Integer communityReport;

    @Column(name = "cost_safety")
    private Double costSafety;

    @Column(name = "cost_speed")
    private Double costSpeed;

    @Column(name = "geometry", columnDefinition = "geometry(LineString, 4326)")
    private LineString geom;
}