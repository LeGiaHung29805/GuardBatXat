package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_ahp_weights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AhpWeight {
    @Id
    @Column(name = "strategy_name", length = 50)
    private String strategyName;

    @Column(name = "w_distance")
    private Double wDistance;

    @Column(name = "w_flood")
    private Double wFlood;

    @Column(name = "w_landslide")
    private Double wLandslide;

    @Column(name = "w_capacity")
    private Double wCapacity;

    @Column(name = "w_bridge")
    private Double wBridge;

    @Column(name = "w_report")
    private Double wReport;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}