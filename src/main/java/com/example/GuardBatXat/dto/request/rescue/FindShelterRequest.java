package com.example.GuardBatXat.dto.request.rescue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindShelterRequest {
    private Double currentLat;
    private Double currentLng;
    private String strategy; // Truyền "safety" (Mặc định) hoặc "rescue" (Cứu hộ khẩn cấp)
}