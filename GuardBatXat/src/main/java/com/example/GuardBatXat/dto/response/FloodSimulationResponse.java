package com.example.GuardBatXat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FloodSimulationResponse {
    private Long buildingId;
    private Double depth;
    private String status;
    private Double lng;
    private Double lat;
}