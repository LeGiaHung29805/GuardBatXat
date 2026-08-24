package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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

    @Column(name = "w_distance", precision = 10, scale = 5)
    private BigDecimal wDistance;

    @Column(name = "w_flood", precision = 10, scale = 5)
    private BigDecimal wFlood;

    @Column(name = "w_landslide", precision = 10, scale = 5)
    private BigDecimal wLandslide;

    @Column(name = "w_capacity", precision = 10, scale = 5)
    private BigDecimal wCapacity;

    @Column(name = "w_bridge", precision = 10, scale = 5)
    private BigDecimal wBridge;

    @Column(name = "w_report", precision = 10, scale = 5)
    private BigDecimal wReport;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
