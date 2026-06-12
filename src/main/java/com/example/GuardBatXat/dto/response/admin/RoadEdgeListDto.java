package com.example.GuardBatXat.dto.response.admin;
import com.example.GuardBatXat.entity.RoadEdge;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadEdgeListDto {
    private Integer edgeKey;
    private Long uNode;
    private Long vNode;
    private Double lengthM;
    private Double avgSlope;
}