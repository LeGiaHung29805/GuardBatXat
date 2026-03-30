package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.LocationCheckRequest;
import com.example.GuardBatXat.dto.response.LocationCheckResponse;
import com.example.GuardBatXat.entity.Building;
import com.example.GuardBatXat.entity.SystemState;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.SystemStateRepository;
import com.example.GuardBatXat.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final BuildingRepository buildingRepository;
    private final SystemStateRepository systemStateRepository;

    @Override
    public LocationCheckResponse checkLocationSafety(LocationCheckRequest request) {
        // Tìm tòa nhà gần nhất với GPS của người dùng
        Optional<Building> buildingOpt = buildingRepository.findNearestBuilding(
                request.getLongitude(), request.getLatitude()
        );

        if (buildingOpt.isEmpty()) {
            return LocationCheckResponse.builder()
                    .isSafe(false).alertLevel("UNKNOWN").message("Khu vực này chưa được lập bản đồ AI.")
                    .build();
        }
        Building b = buildingOpt.get();

        // Lấy Mực nước lũ hiện tại của toàn hệ thống (Từ Module 2 dự báo)
        SystemState state = systemStateRepository.findFirstByOrderByIdDesc();
        double currentFloodLevel = (state != null && state.getActiveFloodLevel() != null)
                ? state.getActiveFloodLevel() : 0.0;

        // TÍNH TOÁN RỦI RO NGẬP LỤT (Dựa theo logic function simulate_flood_risk)
        double floodDepth = Math.max(0, currentFloodLevel - b.getElevationZ());
        String floodStatus = "An toàn";
        if (floodDepth > 2) floodStatus = "Nguy cơ Rất cao (Ngập > 2m)";
        else if (floodDepth > 0) floodStatus = "Nguy cơ Cao (Bị ngập)";
        else if (b.getDistToWater() < 50) floodStatus = "Nguy cơ Cao (Sát mép nước/Sạt lở bờ)";
        else if ((b.getElevationZ() - currentFloodLevel) <= 5) floodStatus = "Nguy cơ Vừa (Sát mép lũ)";

        // TÍNH TOÁN RỦI RO SẠT LỞ (Dựa theo logic function get_combined_landslide_risk)
        double lsProb = b.getLandslideProb() != null ? b.getLandslideProb() : 0.0;
        double waterFactor = b.getDistToWater() < 20 ? 1.0 : (b.getDistToWater() < 50 ? 0.6 : (b.getDistToWater() < 100 ? 0.3 : 0.0));

        // Công thức vật lý tổng hợp: AI (70%) + Thủy văn (30%)
        double combinedLsScore = (lsProb * 0.7) + (waterFactor * 0.3);

        String landslideStatus = "Thấp (An toàn)";
        if (combinedLsScore >= 0.75) landslideStatus = "Rất Cao (Nguy cấp)";
        else if (combinedLsScore >= 0.55) landslideStatus = "Cao";
        else if (combinedLsScore >= 0.35) landslideStatus = "Trung Bình";

        //RA QUYẾT ĐỊNH CUỐI CÙNG (ALERT LEVEL)
        boolean isSafe = (floodDepth == 0) && (combinedLsScore < 0.55);
        String alertLevel = "SAFE";
        String message = "Vị trí của bạn hiện tại đang an toàn.";

        //Nếu ngập sâu trên 2m HOẶC sạt lở nguy cấp -> DANGER
        if (floodDepth > 2 || combinedLsScore >= 0.75) {
            alertLevel = "DANGER";
            message = "CẢNH BÁO KHẨN CẤP: Khu vực nguy hiểm! Có rủi ro thiên tai rất cao. Yêu cầu sơ tán lập tức!";
        }
        //Nếu bắt đầu ngập HOẶC sạt lở mức cao -> WARNING
        else if (floodDepth > 0 || combinedLsScore >= 0.55) {
            alertLevel = "WARNING";
            message = "CẢNH BÁO: Rủi ro cao. Hãy chuẩn bị hành lý khẩn cấp và theo dõi chặt chẽ thông báo từ chính quyền.";
        }

        //Trả về kết quả
        return LocationCheckResponse.builder()
                .isSafe(isSafe)
                .alertLevel(alertLevel)
                .message(message)
                .floodRiskStatus(floodStatus)
                .landslideRiskStatus(landslideStatus)
                .floodDepth(Math.round(floodDepth * 100.0) / 100.0)
                .aiLandslideProb(Math.round(lsProb * 100.0 * 100.0) / 100.0) // Chuyển sang %
                .buildingType(b.getBuildingType())
                .distanceToWater(Math.round(b.getDistToWater() * 100.0) / 100.0)
                .currentElevation(b.getElevationZ())
                .build();
    }
}