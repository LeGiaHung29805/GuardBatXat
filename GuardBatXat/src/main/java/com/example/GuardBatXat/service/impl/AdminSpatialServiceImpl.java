package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.BuildingRequest;
import com.example.GuardBatXat.dto.request.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.RoadEdgeListDto;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.RoadEdgeRepository;
import com.example.GuardBatXat.service.AdminSpatialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSpatialServiceImpl implements AdminSpatialService {

    private final BuildingRepository buildingRepository;
    private final RoadEdgeRepository roadEdgeRepository;

    @Override
    @Transactional
    public void addBuilding(BuildingRequest request) {
        buildingRepository.insertBuildingNative(
                request.getAreaInMeters(), request.getElevationZ(),
                request.getBuildingType(), request.getMaxCapacity(),
                request.getEstimatedPop(), request.getGeomWkt()
        );
    }

    @Override
    @Transactional
    public void updateBuilding(Long id, BuildingRequest request) {
        if (!buildingRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy công trình này!");
        }
        buildingRepository.updateBuildingNative(
                id, request.getAreaInMeters(), request.getBuildingType(),
                request.getMaxCapacity(), request.getEstimatedPop(), request.getGeomWkt()
        );
    }

    @Override
    @Transactional
    public void deleteBuilding(Long id) {
        buildingRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addRoadEdge(RoadEdgeRequest request) {
        roadEdgeRepository.insertRoadEdgeNative(
                request.getUNode(), request.getVNode(), request.getEdgeKey(),
                request.getLengthM(), request.getRoadCapacity(),
                request.getIsBridge(), request.getGeomWkt()
        );
    }
    @Override
    public List<RoadEdgeListDto> getAllRoadEdgesOptimized() {
        return roadEdgeRepository.findAllOptimizedForAdmin();
    }
}