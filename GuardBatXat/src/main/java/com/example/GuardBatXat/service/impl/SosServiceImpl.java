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
}