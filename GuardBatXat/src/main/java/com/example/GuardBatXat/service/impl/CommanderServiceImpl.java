package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.AlertRequest;
import com.example.GuardBatXat.repository.DisasterCommandRepository;
import com.example.GuardBatXat.service.CommanderService;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CommanderServiceImpl implements CommanderService {
    @Autowired
    private DisasterCommandRepository disasterRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 1. Giám sát tổng thể (Heatmap)
    @Override
    public List<Map<String, Object>> getFloodRiskData(BigDecimal level) {
        if (level == null || level.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_FLOOD_LEVEL);
        }
        return disasterRepo.getFloodHeatmap(level);
    }

    @Override
    public List<Map<String, Object>> getLandslideRiskData() {
        return disasterRepo.getLandslideHeatmap();
    }

    // 2. Kích hoạt kịch bản sơ tán
    @Override
    @Transactional
    public String triggerEvacuationScenario(BigDecimal floodLevel) {
        if (floodLevel == null || floodLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_FLOOD_LEVEL);
        }
        String simulationId = UUID.randomUUID().toString();
        // Chạy query insert hàng loạt vào DB
        disasterRepo.executeEvacuationScenario(simulationId, floodLevel);
        return simulationId; // Trả về ID để frontend đem đi truy vấn phân tích
    }
    // 3. Phân tích hậu thiên tai
    @Override
    public List<Map<String, Object>> getPostDisasterAnalysis(String simulationId) {
        List<Map<String, Object>> stats = disasterRepo.getDamageStatistics(simulationId);
        if (stats == null || stats.isEmpty()) {
            throw new AppException(ErrorCode.SIMULATION_NOT_FOUND);
        }
        return stats;
    }

    @Override
    // 4. Phát tin báo động khu vực (Push Notification qua WebSocket)
    public void broadcastEmergencyAlert(AlertRequest request) {
        // Lưu thông báo vào bảng batxat_notifications (Bạn tự implement phần save entity nhé)
        // ... notificationRepository.save(...)

        // Gửi push notification realtime tới những client đang subscribe kênh /topic/alerts
        messagingTemplate.convertAndSend("/topic/alerts", request);
    }




        // 5. Lấy bảng thống kê tổng hợp theo mực nước
    @Override
    public Map<String, Object> getFloodStatistics(BigDecimal floodLevel) {
        // Lấy dữ liệu từ Database
        Map<String, Object> buildingStats = disasterRepo.getBuildingFloodStats(floodLevel);
        Map<String, Object> roadStats = disasterRepo.getRoadFloodStats(floodLevel);

        // Gom chung vào 1 Object để trả về cho Frontend
        Map<String, Object> result = new HashMap<>();
        result.put("flood_level_scenario", floodLevel);

        // Thêm các chỉ số vào (kiểm tra null tránh lỗi)
        if (buildingStats != null) result.putAll(buildingStats);
        if (roadStats != null) result.putAll(roadStats);

        return result;
    }

    @Override

        public Map<String, Object> getDashboardStatistics(BigDecimal floodLevel) {
        Map<String, Object> buildingStats = disasterRepo.getBuildingStatsForDashboard(floodLevel);
        Map<String, Object> roadStats = disasterRepo.getRoadStatsForDashboard(floodLevel);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("kich_ban_muc_nuoc", floodLevel);

        // Lấy số liệu nhà và người
        if (buildingStats != null) {
            result.put("nha_bi_ngap", buildingStats.get("nha_bi_ngap"));
            result.put("nguoi_can_so_tan", buildingStats.get("nguoi_can_so_tan"));

            // Giữ nguyên m², chỉ làm tròn 2 chữ số thập phân
            Object m2Obj = buildingStats.get("dien_tich_ngap_m2");
            if (m2Obj != null) {
                double m2 = Double.parseDouble(m2Obj.toString());


                DecimalFormat df = new DecimalFormat("#,##0.00");
                String formattedM2 = df.format(m2);
                result.put("dien_tich_ngap_m2", formattedM2);
            } else {
                result.put("dien_tich_ngap_m2", 0.00);
            }
        }

        // Lấy số liệu đường
        if (roadStats != null) {
            result.put("duong_bi_chan", roadStats.get("duong_bi_chan"));
        }

        return result;

    }

    @Override
    // API: Kích hoạt lệnh sơ tán khẩn cấp
    public void activateEvacuation(BigDecimal floodLevel) {
        if (floodLevel == null || floodLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_FLOOD_LEVEL);
        }
        // Chuẩn bị gói tin khẩn cấp (Payload)
        Map<String, Object> alertMessage = new java.util.HashMap<>();
        alertMessage.put("type", "EVACUATION_ORDER");
        alertMessage.put("level", floodLevel);
        alertMessage.put("title", "🚨 LỆNH SƠ TÁN KHẨN CẤP");
        alertMessage.put("content", "Nước lũ dự báo lên mốc " + floodLevel + "m. Yêu cầu toàn bộ người dân vùng màu Đỏ và Cam di chuyển đến điểm trú ẩn ngay lập tức!");
        alertMessage.put("timestamp", java.time.LocalDateTime.now().toString());

        // Bắn Broadcast qua WebSocket tới tất cả những ai đang lắng nghe kênh /topic/alerts
        messagingTemplate.convertAndSend("/topic/alerts", alertMessage);

        // (Tùy chọn) Sau này bạn có thể viết thêm code lưu lịch sử vào bảng batxat_notifications ở đây
    }
    @Override
    // API: Trả về data cho Biểu đồ thanh ngang (Nghiêm trọng, Nhẹ...)
    public List<Map<String, Object>> getSeverityChartData(BigDecimal floodLevel) {
        return disasterRepo.getSeverityStats(floodLevel);
    }


    @Override
    // API: Trả về data cho Top 5 Khu Vực ngập nặng nhất
    public List<Map<String, Object>> getTopAffectedAreas(BigDecimal floodLevel) {
        return disasterRepo.getTop5AffectedAreas(floodLevel);
    }

    @Override
    // API: Dữ liệu Line Chart thiệt hại theo thời gian
    public List<Map<String, Object>> getDamageTrendChartData() {
        return disasterRepo.getDamageTrendOverTime();
    }

    @Override
        public List<Map<String, Object>> getDamageByTypeData(BigDecimal floodLevel) {
        return disasterRepo.getDamageByTypeStats(floodLevel);
    }



    @Override
    // API: Gửi thông báo (Ghép khu vực vào Title)
    public void sendAndSaveNotification(Map<String, String> payload) {
        String rawTitle = payload.getOrDefault("title", "THÔNG BÁO TỪ BAN CHỈ HUY");
        String content = payload.get("content");
        String level = payload.getOrDefault("level", "INFO");
        String targetArea = payload.getOrDefault("targetArea", "Tất cả");

        // TRICK: Nhúng tên khu vực vào tiêu đề. VD: "[Thôn Lao Chải] Mưa lớn..."
        String dbTitle = "[" + targetArea + "] " + rawTitle;

        // 1. Lưu xuống DB với Tiêu đề đã được ghép
        disasterRepo.saveAlertHistory(dbTitle, content, level);

        // 2. Bắn Broadcast qua WebSocket (Gửi bóc tách đàng hoàng cho App dễ nhận)
        Map<String, Object> wsMessage = new java.util.HashMap<>();
        wsMessage.put("type", "MANUAL_ALERT");
        wsMessage.put("title", rawTitle);
        wsMessage.put("content", content);
        wsMessage.put("level", level);
        wsMessage.put("targetArea", targetArea);
        wsMessage.put("timestamp", java.time.LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/alerts", wsMessage);
    }

    @Override
    // API: Lấy Lịch sử (Tách khu vực ra khỏi Title trước khi trả về cho Frontend)
    public List<Map<String, Object>> getNotificationHistory() {
        List<Map<String, Object>> rawList = disasterRepo.getRawAlertHistory();
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();

        for (Map<String, Object> row : rawList) {
            // Tạo một Map mới để có thể sửa đổi dữ liệu
            Map<String, Object> processedRow = new java.util.HashMap<>(row);
            String fullTitle = (String) row.get("title");

            // Xử lý cắt chuỗi nếu có định dạng [...]
            if (fullTitle != null && fullTitle.startsWith("[")) {
                int closeBracketIndex = fullTitle.indexOf("]");
                if (closeBracketIndex > 0) {
                    // Lấy phần chữ bên trong ngoặc vuông làm Khu vực
                    String area = fullTitle.substring(1, closeBracketIndex);
                    // Lấy phần chữ phía sau ngoặc vuông làm Tiêu đề thật
                    String realTitle = fullTitle.substring(closeBracketIndex + 1).trim();

                    processedRow.put("target_area", area);
                    processedRow.put("title", realTitle);
                } else {
                    processedRow.put("target_area", "Tất cả");
                }
            } else {
                processedRow.put("target_area", "Tất cả");
            }

            resultList.add(processedRow);
        }
        return resultList;
    }

    @Override
    // Trả về data cho bản đồ Heatmap Ngập lụt (Góc nhìn Chỉ huy)
    public List<Map<String, Object>> getCommanderFloodHeatmapData(BigDecimal floodLevel) {
        return disasterRepo.getCommanderFloodHeatmap(floodLevel);
    }

    @Override
    // Trả về data cho bản đồ Heatmap Sạt lở (Góc nhìn Chỉ huy)
    public List<Map<String, Object>> getCommanderLandslideHeatmapData() {
        return disasterRepo.getCommanderLandslideHeatmap();
    }

    @Override
    public List<Map<String, Object>> getSpatialMapData() {
        return disasterRepo.getMapSpatialData(); // Đổi thành disasterRepo
    }
}



