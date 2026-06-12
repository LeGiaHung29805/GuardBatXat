package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.entity.SosEntity;
import com.example.GuardBatXat.repository.NotificationRepository;
import com.example.GuardBatXat.repository.SosRequestRepository;
import com.example.GuardBatXat.service.RescueService;
import com.example.GuardBatXat.websocket.NotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RescueServiceImpl implements RescueService {

    private final SosRequestRepository sosRequestRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<SosEntity> getAllSosRequests() {
        return sosRequestRepository.findAll();
    }

    @Override
    @Transactional
    public SosEntity acceptSosRequest(Integer id) {
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS ID: " + id));
        sos.setStatus("RESCUING");
        sosRequestRepository.save(sos);

        notificationSender.sendSystemNotification("/topic/alerts", Map.of(
                "type", "SOS_STATUS_UPDATE",
                "sosId", id,
                "status", "RESCUING",
                "message", "Đội cứu hộ đã tiếp nhận nhiệm vụ SOS " + id
        ));

        return sos;
    }

    @Override
    @Transactional
    public SosEntity completeSosRequest(Integer id) {
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS ID: " + id));
        sos.setStatus("COMPLETED");
        sosRequestRepository.save(sos);

        notificationSender.sendSystemNotification("/topic/alerts", Map.of(
                "type", "SOS_STATUS_UPDATE",
                "sosId", id,
                "status", "COMPLETED",
                "message", "Nhiệm vụ cứu hộ SOS " + id + " đã hoàn tất"
        ));

        return sos;
    }

    @Override
    public List<Map<String, Object>> getSosFieldUpdates(Integer sosId) {
        List<Notification> logs = notificationRepository.findByAlertLevelOrderByCreatedAtDesc("RESCUE_LOG_" + sosId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : logs) {
            try {
                Map<String, Object> map = objectMapper.readValue(n.getContent(), Map.class);
                map.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
                map.put("missionId", sosId);
                result.add(map);
            } catch (Exception e) {
                log.error("Lỗi parse Rescue Log", e);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> sendSosFieldUpdate(Integer sosId, Map<String, Object> data) {
        SosEntity sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS ID: " + sosId));

        String status = (String) data.get("status");
        
        try {
            // Chuẩn hóa data
            Double lat = null, lng = null;
            if (data.get("location") instanceof Map) {
                Map<String, Object> loc = (Map<String, Object>) data.get("location");
                if (loc.get("lat") != null) lat = Double.valueOf(loc.get("lat").toString());
                if (loc.get("lng") != null) lng = Double.valueOf(loc.get("lng").toString());
            }

            String images = null;
            if (data.get("images") instanceof List) {
                List<String> imgList = (List<String>) data.get("images");
                images = String.join(",", imgList);
            }

            Map<String, Object> saveData = Map.of(
                "status", status != null ? status : "",
                "message", data.get("message") != null ? data.get("message") : "",
                "gpsLat", lat != null ? lat : 0.0,
                "gpsLng", lng != null ? lng : 0.0,
                "images", images != null ? images : ""
            );

            Notification notif = new Notification();
            notif.setTitle("Field Update SOS " + sosId);
            notif.setContent(objectMapper.writeValueAsString(saveData));
            notif.setAlertLevel("RESCUE_LOG_" + sosId);
            notificationRepository.save(notif);

            if ("arrived".equals(status) || "rescuing".equals(status)) {
                sos.setStatus("RESCUING");
                sosRequestRepository.save(sos);
            } else if ("completed".equals(status)) {
                sos.setStatus("COMPLETED");
                sosRequestRepository.save(sos);
            }

            notificationSender.sendSystemNotification("/topic/alerts", Map.of(
                    "type", "FIELD_UPDATE",
                    "sosId", sosId,
                    "status", status,
                    "message", "Cập nhật hiện trường: " + data.get("message")
            ));

            saveData = new java.util.HashMap<>(saveData);
            saveData.put("missionId", sosId);
            saveData.put("createdAt", java.time.LocalDateTime.now().toString());
            return saveData;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Field Update", e);
        }
    }
}
