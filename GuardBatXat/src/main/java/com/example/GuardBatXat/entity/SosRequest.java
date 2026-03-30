package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_sos_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sender_phone", length = 15)
    private String senderPhone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 20)
    private String status; // OPEN, VERIFIED, RESCUING, COMPLETED

    @Column(name = "gps_lat")
    private Double gpsLat;

    @Column(name = "gps_lng")
    private Double gpsLng;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    // Khóa ngoại liên kết với tòa nhà người dân đang đứng (nếu có)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    // Khóa ngoại liên kết với đội cứu hộ được giao nhiệm vụ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private User assignedTeam;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}