package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_system_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "active_flood_level")
    private Double activeFloodLevel;

    @Column(name = "is_emergency")
    private Boolean isEmergency;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}