package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.entity.SosEntity;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.entity.UserProfile;
import com.example.GuardBatXat.repository.SosRequestRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.SosService;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosServiceImpl implements SosService {

    private final SosRequestRepository sosRequestRepository;
    private final com.example.GuardBatXat.repository.SosUpdateLogRepository sosUpdateLogRepository;
    private final UserRepository userRepository;
    private final NotificationSender notificationSender;

    @Override
    @Transactional
    public void processSosRequest(SosRequest requestDto) {
        log.info("Bắt đầu xử lý yêu cầu SOS...");

        Integer senderId = null;
        String senderName = requestDto.getSenderName();
        String senderPhone = requestDto.getSenderPhone();
        Integer totalPeople = requestDto.getTotalPeople() != null ? requestDto.getTotalPeople() : 1;
        Integer elderlyCount = requestDto.getElderlyCount() != null ? requestDto.getElderlyCount() : 0;
        Integer childrenCount = requestDto.getChildrenCount() != null ? requestDto.getChildrenCount() : 0;

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String identifier = auth.getName();
                User user = userRepository.findByIdentifier(identifier).orElse(null);
                if (user != null) {
                    senderId = user.getUserId();
                    // Fallback to user profile details if present
                    senderName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                    senderPhone = user.getPhoneNumber() != null ? user.getPhoneNumber() : senderPhone;
                    
                    UserProfile profile = user.getUserProfile();
                    if (profile != null) {
                        totalPeople = profile.getTotalMembers() != null ? profile.getTotalMembers() : totalPeople;
                        elderlyCount = profile.getElderlyCount() != null ? profile.getElderlyCount() : elderlyCount;
                        childrenCount = profile.getChildrenCount() != null ? profile.getChildrenCount() : childrenCount;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể lấy thông tin người dùng từ Security Context: {}", e.getMessage());
        }

        com.example.GuardBatXat.entity.SosEntity sos = new com.example.GuardBatXat.entity.SosEntity();
        sos.setSenderPhone(senderPhone);
        sos.setMessage(requestDto.getMessage());
        sos.setGpsLat(requestDto.getLat());
        sos.setGpsLng(requestDto.getLng());
        sos.setSenderName(senderName);
        sos.setTotalPeople(totalPeople);
        sos.setElderlyCount(elderlyCount);
        sos.setChildrenCount(childrenCount);
        sos.setStatus("OPEN");
        if (senderId != null) {
            User u = new User();
            u.setUserId(senderId);
            sos.setSender(u);
        }
        
        try {
            org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory();
            org.locationtech.jts.geom.Point point = factory.createPoint(new org.locationtech.jts.geom.Coordinate(requestDto.getLng(), requestDto.getLat()));
            point.setSRID(4326);
            sos.setGeom(point);
        } catch (Exception e) {
            log.warn("Lỗi set geom Point: {}", e.getMessage());
        }

        SosEntity savedSos = sosRequestRepository.save(sos);

        // Chuẩn bị payload gửi WebSocket
        java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
        wsPayload.put("id", savedSos.getId());
        wsPayload.put("senderName", savedSos.getSenderName());
        wsPayload.put("senderPhone", savedSos.getSenderPhone());
        wsPayload.put("message", savedSos.getMessage());
        wsPayload.put("lat", savedSos.getGpsLat());
        wsPayload.put("lng", savedSos.getGpsLng());
        wsPayload.put("totalPeople", savedSos.getTotalPeople());
        wsPayload.put("status", savedSos.getStatus());

        // Bắn WebSocket tới các tài khoản có role COMMANDER / RESCUE_TEAM
        try {
            notificationSender.sendEmergencyAlert("/topic/emergency", wsPayload);
            log.info("Đã phát tín hiệu SOS Real-time tới trung tâm chỉ huy, ID: {}", savedSos.getId());
        } catch (Exception e) {
            log.error("Lỗi khi phát tín hiệu WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public void updateLiveLocation(com.example.GuardBatXat.dto.request.LiveLocationRequest request) {
        log.info("Cập nhật vị trí Live Location cho {}: [{}, {}]", request.getEntityId(), request.getLat(), request.getLng());
        try {
            notificationSender.sendSystemNotification("/topic/rescue-tracking", request);
        } catch (Exception e) {
            log.error("Lỗi gửi Live Location qua WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public void sendEmergencyChat(com.example.GuardBatXat.dto.request.ChatRequest request) {
        log.info("SOS Chat từ {} (SOS ID: {}): {}", request.getSender(), request.getSosId(), request.getMessage());
        try {
            String destination = "/topic/chat/sos/" + request.getSosId();
            notificationSender.sendSystemNotification(destination, request);
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn Chat qua WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public java.util.List<com.example.GuardBatXat.dto.response.SosResponse> getAllSosRequests() {
        return sosRequestRepository.findAll().stream().map(sos -> com.example.GuardBatXat.dto.response.SosResponse.builder()
                .id(sos.getId())
                .senderName(sos.getSenderName())
                .senderPhone(sos.getSenderPhone())
                .message(sos.getMessage())
                .status(sos.getStatus())
                .gpsLat(sos.getGpsLat())
                .gpsLng(sos.getGpsLng())
                .totalPeople(sos.getTotalPeople())
                .createdAt(sos.getCreatedAt())
                .assignedTo(sos.getAssignedUser() != null ? sos.getAssignedUser().getUsername() : null)
                .build()).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptSosRequest(Integer id, String identifier) {
        SosEntity sos = sosRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        User rescuer = userRepository.findByIdentifier(identifier).orElseThrow(() -> new RuntimeException("Không tìm thấy người cứu hộ"));
        sos.setStatus("RESCUING");
        sos.setAssignedUser(rescuer);
        sosRequestRepository.save(sos);
        
        com.example.GuardBatXat.entity.SosUpdateLog log = com.example.GuardBatXat.entity.SosUpdateLog.builder()
                .sosRequest(sos)
                .updateStatus("ACCEPTED")
                .message("Đội cứu hộ " + rescuer.getFullName() + " đã tiếp nhận.")
                .build();
        sosUpdateLogRepository.save(log);
    }

    @Override
    @Transactional
    public void completeSosRequest(Integer id, String identifier) {
        SosEntity sos = sosRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        sos.setStatus("COMPLETED");
        sosRequestRepository.save(sos);
        
        com.example.GuardBatXat.entity.SosUpdateLog log = com.example.GuardBatXat.entity.SosUpdateLog.builder()
                .sosRequest(sos)
                .updateStatus("COMPLETED")
                .message("Nhiệm vụ cứu hộ đã hoàn thành.")
                .build();
        sosUpdateLogRepository.save(log);
    }

    @Override
    public java.util.List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse> getSosUpdates(Integer id) {
        SosEntity sos = sosRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        return sosUpdateLogRepository.findBySosRequestIdOrderByCreatedAtDesc(sos.getId()).stream().map(log -> 
            com.example.GuardBatXat.dto.response.SosUpdateLogResponse.builder()
                .id(log.getId())
                .missionId(sos.getId())
                .status(log.getUpdateStatus())
                .message(log.getMessage())
                .lat(log.getGpsLat())
                .lng(log.getGpsLng())
                .images(log.getImages() != null ? java.util.Arrays.asList(log.getImages().split(",")) : new java.util.ArrayList<>())
                .timestamp(log.getCreatedAt())
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void addSosUpdate(Integer id, com.example.GuardBatXat.dto.request.SosUpdateLogRequest request, String identifier) {
        SosEntity sos = sosRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        com.example.GuardBatXat.entity.SosUpdateLog log = com.example.GuardBatXat.entity.SosUpdateLog.builder()
                .sosRequest(sos)
                .updateStatus(request.getStatus())
                .message(request.getMessage())
                .gpsLat(request.getLat())
                .gpsLng(request.getLng())
                .images(request.getImages() != null ? String.join(",", request.getImages()) : null)
                .build();
        sosUpdateLogRepository.save(log);
    }
}