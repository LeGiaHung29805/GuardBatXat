package com.example.GuardBatXat.dto.response.admin;
import com.example.GuardBatXat.entity.Building;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageStatResponse {
    private Long totalBuildingsAffected;
    private Double totalAreaAffected;
    private Long estimatedPopDisplaced;
}