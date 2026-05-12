package com.example.GuardBatXat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoutingResponse {
    private String strategyName; // "Ngắn nhất", "An toàn", "Bất chấp"
    private Double totalDistance;
    private Double avgSlope;
    private List<double[]> pathPoints; // Mảng các tọa độ [lat, lng] để vẽ map
}