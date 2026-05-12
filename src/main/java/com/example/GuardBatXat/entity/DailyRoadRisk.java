package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "batxat_daily_road_risk", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"forecast_date", "u_node", "v_node", "key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRoadRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "forecast_date")
    private LocalDate forecastDate;

    @Column(name = "u_node")
    private Long uNode;

    @Column(name = "v_node")
    private Long vNode;

    private Integer key;

    @Column(name = "landslide_prob")
    private Float landslideProb;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;
}