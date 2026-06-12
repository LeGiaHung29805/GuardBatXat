package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.BuildingRequest;
import com.example.GuardBatXat.dto.request.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.RoadEdgeListDto;

import java.util.List;
import java.util.Map;

public interface AdminSpatialService {
    void addBuilding(BuildingRequest request);
    void updateBuilding(Long id, BuildingRequest request);
    void deleteBuilding(Long id);
    void addRoadEdge(RoadEdgeRequest request);
    List<RoadEdgeListDto> getAllRoadEdgesOptimized();
    List<Map<String, Object>> getAllBuildings();
}