package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.EvacuationResponse;
import com.example.GuardBatXat.dto.response.LocationCheckResponse;
import com.example.GuardBatXat.dto.response.SafeHavenProjection;
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
    private final RestTemplate restTemplate = new RestTemplate(); // Dùng để gọi API ngoài
    private final SafeHavenRepository safeHavenRepository;
    private final NotificationSender notificationSender;

    @Override
    public LocationCheckResponse checkLocationSafety(LocationCheckRequest request) {
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        //XỬ LÝ ĐỊA CHỈ: Nếu không có GPS, phải dịch từ chuỗi Address sang Tọa độ
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
        }

        //TÌM NGÔI NHÀ GẦN NHẤT BẰNG AI & POSTGIS
        Optional<Building> buildingOpt = buildingRepository.findNearestBuilding(lng, lat);

        if (buildingOpt.isEmpty()) {
            return LocationCheckResponse.builder()
                    .isSafe(false).alertLevel("UNKNOWN")
                    .message("Khu vực này chưa được AI lập bản đồ rủi ro.")
                    .build();
        }
        Building b = buildingOpt.get();

        //LẤY MỨC LŨ HIỆN TẠI TỪ HỆ THỐNG
        SystemState state = systemStateRepository.findFirstByOrderByIdDesc();
        double currentFloodLevel = (state != null && state.getActiveFloodLevel() != null)
                ? state.getActiveFloodLevel() : 0.0;

        //TÍNH TOÁN RỦI RO NGẬP LỤT
        double floodDepth = Math.max(0, currentFloodLevel - (b.getElevationZ() != null ? b.getElevationZ() : 0));
        String floodStatus = "An toàn";
        if (floodDepth > 2) floodStatus = "Nguy cơ Rất cao (Ngập > 2m)";
        else if (floodDepth > 0) floodStatus = "Nguy cơ Cao (Bị ngập)";
        else if (b.getDistToWater() != null && b.getDistToWater() < 50) floodStatus = "Nguy cơ Cao (Sát mép nước)";
        else if (b.getElevationZ() != null && (b.getElevationZ() - currentFloodLevel) <= 5) floodStatus = "Nguy cơ Vừa (Sát mép lũ)";

        //TÍNH TOÁN RỦI RO SẠT LỞ (Công thức Vật lý: AI 70% + Thủy văn 30%)
        double lsProb = b.getLandslideProb() != null ? b.getLandslideProb() : 0.0;
        double distWater = b.getDistToWater() != null ? b.getDistToWater() : 999;
        double waterFactor = distWater < 20 ? 1.0 : (distWater < 50 ? 0.6 : (distWater < 100 ? 0.3 : 0.0));

        double combinedLsScore = (lsProb * 0.7) + (waterFactor * 0.3);

        String landslideStatus = "Thấp (An toàn)";
        if (combinedLsScore >= 0.75) landslideStatus = "Rất Cao (Nguy cấp)";
        else if (combinedLsScore >= 0.55) landslideStatus = "Cao";
        else if (combinedLsScore >= 0.35) landslideStatus = "Trung Bình";

        //QUYẾT ĐỊNH CẢNH BÁO
        boolean isSafe = (floodDepth == 0) && (combinedLsScore < 0.55);
        String alertLevel = "SAFE";
        String message = "Vị trí của bạn hiện tại đang an toàn.";

        if (floodDepth > 2 || combinedLsScore >= 0.75) {
            alertLevel = "DANGER";
            message = "CẢNH BÁO KHẨN CẤP: Khu vực có rủi ro thiên tai rất cao. Yêu cầu sơ tán lập tức!";
        } else if (floodDepth > 0 || combinedLsScore >= 0.55) {
            alertLevel = "WARNING";
            message = "CẢNH BÁO: Rủi ro cao. Hãy chuẩn bị hành lý và theo dõi thông báo chính quyền.";
        }

        LocationCheckResponse response = LocationCheckResponse.builder()
                .isSafe(isSafe)
                .alertLevel(alertLevel)
                .message(message)
                .floodRiskStatus(floodStatus)
                .landslideRiskStatus(landslideStatus)
                .floodDepth(Math.round(floodDepth * 100.0) / 100.0)
                .aiLandslideProb(Math.round(lsProb * 100.0 * 100.0) / 100.0)
                .buildingType(b.getBuildingType())
                .distanceToWater(Math.round(distWater * 100.0) / 100.0)
                .currentElevation(b.getElevationZ())
                .build();

        // Phát WebSocket nếu vị trí đang kiểm tra thuộc vùng nguy hiểm
        if ("DANGER".equals(alertLevel) || "WARNING".equals(alertLevel)) {
            try {
                notificationSender.sendSystemNotification("/topic/safety-alerts", response);
            } catch (Exception e) {
                log.error("Lỗi khi bắn WebSocket Safety Alert: {}", e.getMessage());
            }
        }

        return response;
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