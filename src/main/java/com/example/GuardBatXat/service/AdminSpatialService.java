package com.example.GuardBatXat.service;
import com.example.GuardBatXat.entity.RoadEdge;
import com.example.GuardBatXat.entity.Building;

import com.example.GuardBatXat.dto.request.admin.BuildingRequest;
import com.example.GuardBatXat.dto.request.admin.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.admin.RoadEdgeListDto;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminSpatialService {
    void addBuilding(BuildingRequest request);
    void updateBuilding(Long id, BuildingRequest request);
    void deleteBuilding(Long id);
    void addRoadEdge(RoadEdgeRequest request);
    Page<RoadEdgeListDto> getRoadEdges(String search, Pageable pageable);
    Page<Map<String, Object>> getBuildings(String search, Pageable pageable);
}
