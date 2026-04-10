package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.AlertRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CommanderService {


    // 1. Giám sát tổng thể (Heatmap)
     List<Map<String, Object>> getFloodRiskData(BigDecimal level) ;

     List<Map<String, Object>> getLandslideRiskData() ;

     String triggerEvacuationScenario(BigDecimal floodLevel);

     List<Map<String, Object>> getPostDisasterAnalysis(String simulationId) ;


     void broadcastEmergencyAlert(AlertRequest request) ;


     Map<String, Object> getFloodStatistics(BigDecimal floodLevel) ;

     Map<String, Object> getDashboardStatistics(BigDecimal floodLevel) ;


     void activateEvacuation(BigDecimal floodLevel) ;



    // API: Trả về data cho Biểu đồ thanh ngang (Nghiêm trọng, Nhẹ...)
     List<Map<String, Object>> getSeverityChartData(BigDecimal floodLevel) ;
    // API: Trả về data cho Top 5 Khu Vực ngập nặng nhất
     List<Map<String, Object>> getTopAffectedAreas(BigDecimal floodLevel) ;

    // API: Dữ liệu Line Chart thiệt hại theo thời gian
     List<Map<String, Object>> getDamageTrendChartData() ;


     List<Map<String, Object>> getDamageByTypeData(BigDecimal floodLevel) ;

    // API: Gửi thông báo (Ghép khu vực vào Title)
     void sendAndSaveNotification(Map<String, String> payload) ;

    // API: Lấy Lịch sử (Tách khu vực ra khỏi Title trước khi trả về cho Frontend)
     List<Map<String, Object>> getNotificationHistory() ;


    // Trả về data cho bản đồ Heatmap Ngập lụt (Góc nhìn Chỉ huy)
     List<Map<String, Object>> getCommanderFloodHeatmapData(BigDecimal floodLevel) ;

    // Trả về data cho bản đồ Heatmap Sạt lở (Góc nhìn Chỉ huy)
     List<Map<String, Object>> getCommanderLandslideHeatmapData() ;


     List<Map<String, Object>> getSpatialMapData();
}