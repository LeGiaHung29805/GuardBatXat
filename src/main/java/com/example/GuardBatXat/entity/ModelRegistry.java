package com.example.GuardBatXat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batxat_model_registry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(length = 50)
    private String algorithm;

    @Column(name = "model_target", nullable = false, length = 50)
    private String modelTarget;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accuracy_test")
    private Double accuracyTest;

    @Column(name = "cv_score_mean")
    private Double cvScoreMean;

    @Column(name = "feat_imp_elevation")
    private Double featImpElevation;

    @Column(name = "feat_imp_slope")
    private Double featImpSlope;

    @Column(name = "feat_imp_water")
    private Double featImpWater;

    @Column(name = "feat_imp_precip")
    private Double featImpPrecip;

    @Column(name = "feat_imp_soil")
    private Double featImpSoil;

    @Column(name = "model_path", nullable = false, columnDefinition = "TEXT")
    private String modelPath;

    @Column(name = "scaler_path", nullable = false, columnDefinition = "TEXT")
    private String scalerPath;

    @Column(name = "is_active")
    private Boolean isActive;
}