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
public class SosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User sender;

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
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point geom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User assignedUser;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "total_people")
    private Integer totalPeople;

    @Column(name = "elderly_count")
    private Integer elderlyCount;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}