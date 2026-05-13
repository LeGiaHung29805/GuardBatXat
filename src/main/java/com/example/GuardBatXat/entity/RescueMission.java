package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.LineString;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_rescue_missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RescueMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Integer missionId;

    @Column(name = "request_time", insertable = false, updatable = false)
    private LocalDateTime requestTime;

    @Column(name = "start_lat")
    private Double startLat;

    @Column(name = "start_lng")
    private Double startLng;

    @Column(name = "end_lat")
    private Double endLat;

    @Column(name = "end_lng")
    private Double endLng;

    @Column(name = "preferred_strategy", length = 50)
    private String preferredStrategy;

    @Column(name = "total_dist_km")
    private Double totalDistKm;

    @Column(name = "avg_slope")
    private Double avgSlope;

    @Column(name = "max_slope")
    private Double maxSlope;

    @Column(name = "dangerous_segments_count")
    private Integer dangerousSegmentsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id")
    private SosEntity sosRequest;

    // Rescue team assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(name = "status", length = 50, nullable = false)
    private String status = "pending"; // pending, accepted, in_progress, completed, failed

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "route_geom", columnDefinition = "geometry(LineString, 4326)")
    private LineString routeGeom;
}