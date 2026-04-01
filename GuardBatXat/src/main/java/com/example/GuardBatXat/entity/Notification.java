package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    private Integer notifyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_role_id")
    private Role targetRole;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "alert_level", length = 200)
    private String alertLevel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}