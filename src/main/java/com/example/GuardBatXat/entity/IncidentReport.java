package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_incident_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(name = "reporter_name", length = 100)
    private String reporterName;

    @Column(name = "reporter_phone", length = 15)
    private String reporterPhone;

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType;

    @Column(name = "impact_level", nullable = false, length = 20)
    private String impactLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String images;

    @Column(name = "gps_lat", nullable = false)
    private Double gpsLat;

    @Column(name = "gps_lng", nullable = false)
    private Double gpsLng;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
