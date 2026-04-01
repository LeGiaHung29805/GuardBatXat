package com.example.GuardBatXat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRequest {
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
}