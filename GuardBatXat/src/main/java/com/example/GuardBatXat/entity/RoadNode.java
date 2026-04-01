package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "batxat_road_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoadNode {
    @Id
    @Column(name = "node_id")
    private Long nodeId;

    private Double x;
    private Double y;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;
}