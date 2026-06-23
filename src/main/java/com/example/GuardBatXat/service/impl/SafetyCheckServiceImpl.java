package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;
import com.example.GuardBatXat.repository.SafetyCheckRepository;
import com.example.GuardBatXat.service.SafetyCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.example.GuardBatXat.websocket.NotificationSender;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyCheckServiceImpl implements SafetyCheckService {

    private final SafetyCheckRepository safetyCheckRepository;
    private final NotificationSender notificationSender;

    @Override
    public LocationCheckResponse evaluateLocationSafety(LocationCheckRequest request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("Hệ thống cần tọa độ GPS để định vị.");
        }

        // 1. Gọi Database thông qua Repository
        Double floodLevelDouble = safetyCheckRepository.getCurrentSystemFloodLevel();
        BigDecimal currentFloodLevel = BigDecimal.valueOf(floodLevelDouble);

        List<Map<String, Object>> results = safetyCheckRepository.findSafetyDataByLocation(
                request.getLatitude(), request.getLongitude(), currentFloodLevel);

        // 2. Logic xử lý kết quả trống
        if (results.isEmpty()) {
            return LocationCheckResponse.builder()
                    .isSafe(true)
                    .alertLevel("SAFE")
                    .message("Bạn đang ở khu vực ngoài trời hoặc không có công trình được ghi nhận. Tạm thời không có cảnh báo rủi ro.")
                    .floodRiskStatus("Không xác định")
                    .landslideRiskStatus("Không xác định")
                    .floodDepth(0.0).aiLandslideProb(0.0)
                    .buildingType("Khu vực trống").distanceToWater(-1.0).currentElevation(0.0)
                    .build();
        }

        // 3. Bóc tách dữ liệu
        Map<String, Object> row = results.get(0);
        double floodDepth = ((Number) row.get("flood_depth")).doubleValue();
        double aiLandslideProb = ((Number) row.get("ai_landslide_prob")).doubleValue();
        double aiFloodProb = ((Number) row.get("ai_flood_prob")).doubleValue(); // LẤY DATA MỚI
        String floodStatus = (String) row.get("flood_status");
        String landslideStatus = (String) row.get("landslide_status");
        double distToWater = row.get("dist_to_water") != null ? ((Number) row.get("dist_to_water")).doubleValue() : -1.0;

        // 4. Đánh giá độ an toàn (Business Logic)
        boolean isSafe = true;
        String alertLevel = "SAFE";
        String message = "Khu vực an toàn. Không có cảnh báo ngập lụt hay sạt lở tại công trình này.";

        boolean isDanger = floodStatus.contains("Nguy cơ Rất cao") ||
                floodStatus.contains("Nguy cơ Cao") ||
                "Rất Cao (Nguy cấp)".equals(landslideStatus) ||
                "Cao".equals(landslideStatus);

        boolean isWarning = floodDepth > 0 ||
                "Trung Bình".equals(landslideStatus) ||
                floodStatus.contains("Nguy cơ Vừa") ||
                (aiFloodProb > 0.6 && distToWater > 0 && distToWater < 50.0); // LOGIC MỚI BỔ SUNG

        if (isDanger) {
            isSafe = false;
            alertLevel = "DANGER";
            message = "CẢNH BÁO ĐỎ! Công trình bạn đang đứng nằm trong vùng rủi ro cực kỳ nguy hiểm. Hãy tìm đường sơ tán ngay lập tức!";
        } else if (isWarning) {
            isSafe = false;
            alertLevel = "WARNING";
            message = "CHÚ Ý! Khu vực có rủi ro tiềm ẩn. " +
                    (aiFloodProb > 0.6 && distToWater < 50.0 && floodDepth == 0 ? "AI dự báo nguy cơ lũ lụt hôm nay rất cao và nhà bạn ở gần nguồn nước." : "Hãy theo dõi sát sao tình hình và chuẩn bị phương án di dời.");
        }

        LocationCheckResponse response = LocationCheckResponse.builder()
                .isSafe(isSafe)
                .alertLevel(alertLevel)
                .message(message)
                .floodRiskStatus(floodStatus)
                .landslideRiskStatus(landslideStatus)
                .floodDepth(Math.round(floodDepth * 100.0) / 100.0)
                .aiLandslideProb(Math.round(aiLandslideProb * 100.0) / 100.0)
                .aiFloodProb(Math.round(aiFloodProb * 100.0) / 100.0)
                .buildingType((String) row.get("building_type"))
                .maxCapacity(row.get("max_capacity") != null ? ((Number) row.get("max_capacity")).intValue() : 0)
                .currentOccupancy(row.get("current_occupancy") != null ? ((Number) row.get("current_occupancy")).intValue() : 0)
                .distanceToWater(distToWater)
                .currentElevation(row.get("elevation_z") != null ? ((Number) row.get("elevation_z")).doubleValue() : 0.0)
                .build();

        // 5. Bắn thông báo thống kê Live Safety Check cho mọi trường hợp
        try {
            notificationSender.sendSystemNotification("/topic/safety-stats", response);
        } catch (Exception e) {
            log.error("Lỗi gửi thống kê an toàn qua WebSocket: {}", e.getMessage());
        }

        // 6. Bắn thông báo cảnh báo nguy hiểm lên màn hình trực ban
        if (!isSafe && ("DANGER".equals(alertLevel) || "WARNING".equals(alertLevel))) {
            try {
                notificationSender.sendSystemNotification("/topic/safety-alerts", response);
            } catch (Exception e) {
                log.error("Lỗi gửi cảnh báo an toàn qua WebSocket: {}", e.getMessage());
            }
        }

        return response;
    }
}