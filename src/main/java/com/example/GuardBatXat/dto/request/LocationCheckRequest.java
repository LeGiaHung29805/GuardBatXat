package com.example.GuardBatXat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCheckRequest {
    private Double latitude;
    private Double longitude;
    private String address;
}