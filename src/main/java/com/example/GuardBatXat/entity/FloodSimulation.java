package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batxat_flood_simulation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FloodSimulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "simulation_id", nullable = false)
    private UUID simulationId;

    @Column(name = "input_level", nullable = false)
    private Double inputLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "depth_impact")
    private Double depthImpact;

    @Column(name = "risk_status", length = 100)
    private String riskStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}