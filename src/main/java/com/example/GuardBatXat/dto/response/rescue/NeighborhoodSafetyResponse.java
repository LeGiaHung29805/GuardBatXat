package com.example.GuardBatXat.dto.response.rescue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeighborhoodSafetyResponse {
    private Integer totalBuildings;
    private Integer safeBuildings;
    private Integer warningBuildings;
    private Integer dangerBuildings;
    private Double averageElevation;
    private Double maxFloodDepth;
    private List<NeighborhoodBuildingDto> buildings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NeighborhoodBuildingDto {
        private Long id;
        private String buildingType;
        private Double elevationZ;
        private String geomWkt;
        private Double floodDepth;
        private String floodStatus;
        private Double aiLandslideProb;
        private String landslideStatus;
        private String alertLevel;
        private Boolean isSafe;
    }
}
