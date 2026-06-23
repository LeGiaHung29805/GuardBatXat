package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.websocket.NotificationSender;
import com.example.GuardBatXat.entity.RoadEdge;
import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.entity.Building;

import com.example.GuardBatXat.dto.request.admin.BuildingRequest;
import com.example.GuardBatXat.dto.request.admin.RoadEdgeRequest;
import com.example.GuardBatXat.dto.response.admin.RoadEdgeListDto;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.RoadEdgeRepository;
import com.example.GuardBatXat.service.AdminSpatialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSpatialServiceImpl implements AdminSpatialService {

    private final BuildingRepository buildingRepository;
    private final RoadEdgeRepository roadEdgeRepository;
    private final com.example.GuardBatXat.websocket.NotificationSender notificationSender;

    @Override
    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
    public void addBuilding(BuildingRequest request) {
        buildingRepository.insertBuildingNative(
                request.getAreaInMeters(), request.getElevationZ(),
                request.getBuildingType(), request.getMaxCapacity(),
                request.getEstimatedPop(), request.getGeomWkt()
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "buildings", allEntries = true)
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
    @CacheEvict(value = "buildings", allEntries = true)
    public void deleteBuilding(Long id) {
        buildingRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roadEdges", allEntries = true)
    public void addRoadEdge(RoadEdgeRequest request) {
        roadEdgeRepository.insertRoadEdgeNative(
                request.getUNode(), request.getVNode(), request.getEdgeKey(),
                request.getLengthM(), request.getRoadCapacity(),
                request.getIsBridge(), request.getGeomWkt()
        );
        try {
            notificationSender.sendSystemNotification("/topic/route-alerts", 
                "Mạng lưới giao thông vừa có sự thay đổi (Thêm/Sửa đường). Hệ thống đang tự động định tuyến lại các lộ trình bị ảnh hưởng.");
        } catch (Exception e) {
            System.err.println("Lỗi gửi cảnh báo route-alerts: " + e.getMessage());
        }
    }
    @Override
    @Cacheable(value = "roadEdges", key = "'all'")
    public List<RoadEdgeListDto> getAllRoadEdgesOptimized() {
        return roadEdgeRepository.findAllOptimizedForAdmin();
    }

    @Override
    @Cacheable(value = "buildings", key = "'all'")
    public List<Map<String, Object>> getAllBuildings() {
        List<Map<String, Object>> safeData = new ArrayList<>();
        buildingRepository.findAll().forEach(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("buildingType", b.getBuildingType());
            map.put("areaInMeters", b.getAreaInMeters());
            map.put("maxCapacity", b.getMaxCapacity());
            safeData.add(map);
        });
        return safeData;
    }
}