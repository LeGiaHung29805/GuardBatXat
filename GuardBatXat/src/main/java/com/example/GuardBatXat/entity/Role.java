package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "batxat_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;

    @Column(columnDefinition = "TEXT")
    private String description;
}