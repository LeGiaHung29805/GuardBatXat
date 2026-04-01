package com.example.GuardBatXat.dto.request;

import lombok.Data;

@Data
public class LocationCheckRequest {
    private Double latitude;
    private Double longitude;
    private String address;
}