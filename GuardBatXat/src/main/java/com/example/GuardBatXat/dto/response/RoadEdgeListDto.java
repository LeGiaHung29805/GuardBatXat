package com.example.GuardBatXat.dto.response;

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