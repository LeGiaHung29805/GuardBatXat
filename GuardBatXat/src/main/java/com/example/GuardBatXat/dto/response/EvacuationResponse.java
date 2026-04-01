package com.example.GuardBatXat.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EvacuationResponse {
    private String message;
    private List<SafeHavenProjection> nearestHavens; // Danh sách 3 điểm gần nhất
}