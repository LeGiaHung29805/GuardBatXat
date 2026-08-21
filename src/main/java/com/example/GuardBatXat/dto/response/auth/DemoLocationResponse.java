package com.example.GuardBatXat.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoLocationResponse {
    private Long buildingId;
    private Double latitude;
    private Double longitude;
    private String source;
}
