package com.example.GuardBatXat.dto.response;

public class HeatmapPointResponse {
    private Double lat;
    private Double lng;
    private Double weight; // % Rủi ro hoặc Độ sâu ngập

    public HeatmapPointResponse(Double lat, Double lng, Double weight) {
        this.lat = lat;
        this.lng = lng;
        this.weight = weight;
    }

}

