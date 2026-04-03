package com.example.GuardBatXat.dto.response;

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