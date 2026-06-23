package com.example.GuardBatXat.dto.response.commander;
import com.example.GuardBatXat.entity.SafeHaven;
import com.example.GuardBatXat.dto.response.rescue.SafeHavenProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvacuationResponse {
    private String message;
    private List<SafeHavenProjection> nearestHavens; // Danh sách 3 điểm gần nhất
}