package com.example.GuardBatXat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingCompareResponse {
    private List<double[]> shortestPath; // Nét đứt đỏ
    private List<double[]> safetyPath;   // Nét liền xanh
    private List<double[]> rescuePath;   // Nét liền vàng
}