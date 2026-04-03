package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.BuildingRequest;
import com.example.GuardBatXat.dto.request.RoadEdgeRequest;

public interface AdminSpatialService {
    void addBuilding(BuildingRequest request);
    void updateBuilding(Long id, BuildingRequest request);
    void deleteBuilding(Long id);
    void addRoadEdge(RoadEdgeRequest request);
}