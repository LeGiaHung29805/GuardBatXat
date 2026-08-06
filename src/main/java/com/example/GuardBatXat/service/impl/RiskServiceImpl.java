package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.entity.SafeHaven;
import com.example.GuardBatXat.entity.Notification;

import com.example.GuardBatXat.dto.request.rescue.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.commander.EvacuationResponse;
import com.example.GuardBatXat.dto.response.rescue.LocationCheckResponse;
import com.example.GuardBatXat.dto.response.rescue.SafeHavenProjection;
import com.example.GuardBatXat.entity.Building;
import com.example.GuardBatXat.entity.SystemState;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.SafeHavenRepository;
import com.example.GuardBatXat.repository.SystemStateRepository;
import com.example.GuardBatXat.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.GuardBatXat.websocket.NotificationSender;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final BuildingRepository buildingRepository;
    private final SystemStateRepository systemStateRepository;
    private final RestTemplate restTemplate;
    private final SafeHavenRepository safeHavenRepository;
    private final NotificationSender notificationSender;
    private final com.example.GuardBatXat.service.SafetyCheckService safetyCheckService;

    @Override
    public LocationCheckResponse checkLocationSafety(LocationCheckRequest request) {
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        // XỬ LÝ ĐỊA CHỈ: Nếu không có GPS, phải dịch từ chuỗi Address sang Tọa độ
        if (lat == null || lng == null) {
            if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp tọa độ GPS hoặc Địa chỉ hợp lệ.");
            }
            double[] coords = geocodeAddress(request.getAddress());
            if (coords == null) {
                return LocationCheckResponse.builder()
                        .isSafe(false).alertLevel("UNKNOWN")
                        .message("Không thể tìm thấy vị trí từ địa chỉ bạn nhập. Hãy thử dùng GPS.")
                        .build();
            }
            lat = coords[0];
            lng = coords[1];
            request.setLatitude(lat);
            request.setLongitude(lng);
        }

        // Ủy quyền trực tiếp cho SafetyCheckService để đồng nhất logic!
        return safetyCheckService.evaluateLocationSafety(request);
    }

    /**
     * Hàm gọi API OpenStreetMap để dịch chuỗi Địa chỉ thành tọa độ GPS
     */
    private double[] geocodeAddress(String address) {
        try {
            // Gọi API Nominatim (Khuyến nghị thêm huyện Bát Xát vào đuôi để tìm chính xác hơn)
            String url = "https://nominatim.openstreetmap.org/search?q=" + address + ", Bát Xát, Lào Cai&format=json&limit=1";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> location = (Map<String, Object>) response.getBody().get(0);
                double lat = Double.parseDouble(location.get("lat").toString());
                double lng = Double.parseDouble(location.get("lon").toString());
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            log.error("Lỗi khi tìm tọa độ từ địa chỉ: {}", e.getMessage());
        }
        return null;
    }
    @Override
    public EvacuationResponse findNearestSafeHavens(LocationCheckRequest request) {
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        // 1. XỬ LÝ GEOLOCATION: Nếu dùng địa chỉ thì chuyển sang Tọa độ (Hàm geocodeAddress viết ở phần trước)
        if (lat == null || lng == null) {
            if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Cần tọa độ GPS hoặc Địa chỉ để tìm điểm sơ tán.");
            }
            double[] coords = geocodeAddress(request.getAddress());
            if (coords == null) {
                return EvacuationResponse.builder()
                        .message("Không thể dịch địa chỉ để tìm điểm sơ tán. Vui lòng dùng GPS.")
                        .build();
            }
            lat = coords[0];
            lng = coords[1];
        }

        // 2. TÌM TỐI ĐA 3 ĐIỂM SƠ TÁN GẦN NHẤT CÒN CHỖ TRỐNG
        List<SafeHavenProjection> havens = safeHavenRepository.findNearestAvailableHavens(lng, lat, 3);

        if (havens.isEmpty()) {
            return EvacuationResponse.builder()
                    .message("CẢNH BÁO: Hiện không có điểm sơ tán nào xung quanh còn khả năng tiếp nhận hoặc có thể tiếp cận!")
                    .build();
        }

        return EvacuationResponse.builder()
                .message("Đã tìm thấy " + havens.size() + " điểm sơ tán an toàn gần bạn.")
                .nearestHavens(havens)
                .build();
    }
}