package com.example.GuardBatXat.dto.request.admin;
import com.example.GuardBatXat.entity.RoadEdge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadEdgeRequest {
    @NotNull private Long uNode;
    @NotNull private Long vNode;
    @NotNull private Integer edgeKey;
    private Double lengthM;
    private Integer roadCapacity;
    private Integer isBridge;

    @NotBlank(message = "Tọa độ tuyến đường không được để trống")
    private String geomWkt; // Ví dụ: "LINESTRING(103.8 22.5, 103.9 22.6)"
}