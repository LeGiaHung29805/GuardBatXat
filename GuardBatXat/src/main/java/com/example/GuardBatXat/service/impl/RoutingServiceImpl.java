package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.RoutingRequest;
import com.example.GuardBatXat.dto.response.RoutingResponse;
import com.example.GuardBatXat.repository.RoadNodeRepository;
import com.example.GuardBatXat.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final RoadNodeRepository roadNodeRepository;

    @Override
    public RoutingResponse findOptimalRoute(String strategyName, RoutingRequest request) {
        // 1. Ánh xạ tọa độ người dùng bấm trên bản đồ thành Node giao thông
        Long startNode = roadNodeRepository.findNearestNode(request.getStartLat(), request.getStartLng());
        Long endNode = roadNodeRepository.findNearestNode(request.getEndLat(), request.getEndLng());

        if (startNode == null || endNode == null) {
            throw new RuntimeException("Khu vực này chưa có dữ liệu mạng lưới đường bộ!");
        }

        /* * 2. TÍCH HỢP PGROUTING (VÙNG GIAO VIỆC CHO LEADER)
         * Tại đây, bạn sẽ gọi một Native Query chọc vào hàm pgRouting của Leader.
         * Ví dụ: roadEdgeRepository.calculateRoutePgRouting(startNode, endNode, strategyName);
         * Vì hàm pgRouting phụ thuộc vào cách cấu hình thư viện C của PostgreSQL,
         * dưới đây là dữ liệu trả về tạm thời (Mock Data) để bạn có thể test ngay giao diện Frontend.
         */

        List<double[]> mockPath = new ArrayList<>();
        mockPath.add(new double[]{request.getStartLat(), request.getStartLng()});
        mockPath.add(new double[]{(request.getStartLat() + request.getEndLat()) / 2, (request.getStartLng() + request.getEndLng()) / 2});
        mockPath.add(new double[]{request.getEndLat(), request.getEndLng()});

        return RoutingResponse.builder()
                .strategyName(strategyName)
                .totalDistance(12.5) // Giả định 12.5 km
                .avgSlope(8.2)       // Giả định độ dốc trung bình
                .pathPoints(mockPath)
                .build();
    }
}