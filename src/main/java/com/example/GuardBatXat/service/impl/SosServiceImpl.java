package com.example.GuardBatXat.service.impl;
import com.example.GuardBatXat.repository.SosUpdateLogRepository;
import com.example.GuardBatXat.entity.SosUpdateLog;
import com.example.GuardBatXat.entity.Notification;
import com.example.GuardBatXat.dto.response.rescue.SosUpdateLogResponse;
import com.example.GuardBatXat.dto.request.rescue.SosUpdateLogRequest;
import com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest;
import com.example.GuardBatXat.dto.request.rescue.ChatRequest;

import com.example.GuardBatXat.dto.request.rescue.SosRequest;
import com.example.GuardBatXat.dto.response.rescue.SosResponse;
import com.example.GuardBatXat.entity.SosEntity;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.SosRequestRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.SosService;
import com.example.GuardBatXat.websocket.NotificationSender;
import com.example.GuardBatXat.repository.NotificationRepository;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosServiceImpl implements SosService {

    private final SosRequestRepository sosRequestRepository;
    private final UserRepository userRepository;
    private final com.example.GuardBatXat.repository.SosUpdateLogRepository sosUpdateLogRepository;
    private final NotificationSender notificationSender;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Integer processSosRequest(SosRequest requestDto, String identifier) {
        log.info("Bắt đầu xử lý yêu cầu SOS từ SĐT: {}", requestDto.getSenderPhone());

        Integer senderId = null;
        if (identifier != null && !"anonymousUser".equals(identifier)) {
            User user = userRepository.findByIdentifier(identifier).orElse(null);
            if (user != null) {
                senderId = user.getUserId();
                if (requestDto.getSenderPhone() == null || requestDto.getSenderPhone().trim().isEmpty()) {
                    requestDto.setSenderPhone(user.getUsername());
                }
                if (requestDto.getSenderName() == null || requestDto.getSenderName().trim().isEmpty()) {
                    requestDto.setSenderName(user.getFullName());
                }
            }
        }

        Integer generatedId = sosRequestRepository.insertSosRequestNative(
                requestDto.getSenderPhone(),
                requestDto.getMessage(),
                requestDto.getLat(),
                requestDto.getLng(),
                requestDto.getSenderName() != null ? requestDto.getSenderName() : "Người dân",
                requestDto.getTotalPeople() != null ? requestDto.getTotalPeople() : 1,
                requestDto.getElderlyCount() != null ? requestDto.getElderlyCount() : 0,
                requestDto.getChildrenCount() != null ? requestDto.getChildrenCount() : 0,
                senderId
        );

        // Bắn WebSocket tới các tài khoản có role COMMANDER / RESCUE_TEAM
        try {
            requestDto.setId(generatedId);
            notificationSender.sendEmergencyAlert("/topic/emergency", requestDto);
            log.info("Đã phát tín hiệu SOS Real-time tới trung tâm chỉ huy.");
        } catch (Exception e) {
            log.error("Lỗi khi phát tín hiệu WebSocket: {}", e.getMessage());
        }

        return generatedId;
    }

    @Override
    public void updateLiveLocation(com.example.GuardBatXat.dto.request.rescue.LiveLocationRequest request) {
        log.info("Cập nhật vị trí Live Location cho {}: [{}, {}]", request.getEntityId(), request.getLat(), request.getLng());
        try {
            notificationSender.sendSystemNotification("/topic/rescue-tracking", request);
        } catch (Exception e) {
            log.error("Lỗi gửi Live Location qua WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public void sendEmergencyChat(com.example.GuardBatXat.dto.request.rescue.ChatRequest request) {
        log.info("SOS Chat từ {} (SOS ID: {}): {}", request.getSender(), request.getSosId(), request.getMessage());
        try {
            String destination = "/topic/chat/sos/" + request.getSosId();
            notificationSender.sendSystemNotification(destination, request);
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn Chat qua WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public List<SosResponse> getAllSosRequests() {
        List<SosEntity> entities = sosRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return entities.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptSosRequest(Integer id, String identifier) {
        if (identifier == null || "anonymousUser".equals(identifier)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        sos.setStatus("RESCUING");
        sos.setAssignedUser(user);
        sosRequestRepository.save(sos);

        // Lưu thông báo cứu hộ cá nhân tới người gửi SOS
        if (sos.getSender() != null) {
            Notification notification = new Notification();
            notification.setTitle("Đội cứu hộ đang di chuyển");
            notification.setContent("Đội cứu hộ (" + user.getFullName() + ") đã tiếp nhận yêu cầu SOS của bạn và đang di chuyển tới vị trí của bạn.");
            notification.setAlertLevel("Cứu hộ");
            notification.setTargetUser(sos.getSender());
            notificationRepository.save(notification);
        }

        // Gửi WebSocket thông báo riêng tư đến user này
        try {
            java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
            wsPayload.put("type", "MANUAL_ALERT");
            wsPayload.put("title", "Đội cứu hộ đang di chuyển");
            wsPayload.put("content", "Đội cứu hộ (" + user.getFullName() + ") đã tiếp nhận yêu cầu SOS của bạn và đang di chuyển tới vị trí của bạn.");
            if (sos.getSender() != null) {
                wsPayload.put("targetUser", sos.getSender().getUserId());
            }
            wsPayload.put("targetSosId", sos.getId());
            wsPayload.put("targetPhone", sos.getSenderPhone());
            notificationSender.sendSystemNotification("/topic/alerts", wsPayload);
        } catch (Exception e) {
            log.error("Lỗi gửi WS cảnh báo cứu hộ: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void completeSosRequest(Integer id, String identifier) {
        if (identifier == null || "anonymousUser".equals(identifier)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));
        
        // Kiểm tra xem người hoàn thành có phải là người đã nhận không
        if (sos.getAssignedUser() == null || !sos.getAssignedUser().getUsername().equals(identifier)) {
            // Có thể bỏ qua kiểm tra này nếu muốn các thành viên khác trong đội có thể đóng
        }
        
        sos.setStatus("COMPLETED");
        sosRequestRepository.save(sos);
    }

    private SosResponse mapToResponse(SosEntity entity) {
        return SosResponse.builder()
                .id(entity.getId())
                .senderName(entity.getSenderName() != null ? entity.getSenderName() : "Người dân")
                .senderPhone(entity.getSenderPhone())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .gpsLat(entity.getGpsLat())
                .gpsLng(entity.getGpsLng())
                .totalPeople(entity.getTotalPeople() != null ? entity.getTotalPeople() : 1)
                .createdAt(entity.getCreatedAt())
                .assignedTo(entity.getAssignedUser() != null ? entity.getAssignedUser().getFullName() : null)
                .build();
    }

    @Override
    public List<com.example.GuardBatXat.dto.response.rescue.SosUpdateLogResponse> getSosUpdates(Integer sosId) {
        List<com.example.GuardBatXat.entity.SosUpdateLog> logs = sosUpdateLogRepository.findBySosRequestIdOrderByCreatedAtDesc(sosId);
        return logs.stream().map(log -> {
            List<String> imagesList = null;
            if (log.getImages() != null && !log.getImages().isEmpty()) {
                imagesList = java.util.Arrays.asList(log.getImages().split(",,,"));
            }
            return com.example.GuardBatXat.dto.response.rescue.SosUpdateLogResponse.builder()
                    .id(log.getId())
                    .missionId(sosId)
                    .status(log.getUpdateStatus())
                    .message(log.getMessage())
                    .lat(log.getGpsLat())
                    .lng(log.getGpsLng())
                    .images(imagesList)
                    .timestamp(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addSosUpdate(Integer sosId, com.example.GuardBatXat.dto.request.rescue.SosUpdateLogRequest request, String identifier) {
        if (identifier == null || "anonymousUser".equals(identifier)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        SosEntity sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        String imagesJoined = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imagesJoined = String.join(",,,", request.getImages());
        }

        com.example.GuardBatXat.entity.SosUpdateLog logUpdate = com.example.GuardBatXat.entity.SosUpdateLog.builder()
                .sosRequest(sos)
                .updateStatus(request.getStatus())
                .message(request.getMessage())
                .gpsLat(request.getLat())
                .gpsLng(request.getLng())
                .images(imagesJoined)
                .build();

        sosUpdateLogRepository.save(logUpdate);

        // Lưu thông báo cứu hộ cá nhân tới người gửi SOS khi có cập nhật mới
        if (sos.getSender() != null) {
            Notification notification = new Notification();
            notification.setTitle("Cập nhật cứu hộ");
            notification.setContent("Đội cứu hộ cập nhật trạng thái: " + request.getMessage());
            notification.setAlertLevel("Cứu hộ");
            notification.setTargetUser(sos.getSender());
            notificationRepository.save(notification);
        }

        // Gửi WebSocket thông báo riêng tư đến user này
        try {
            java.util.Map<String, Object> wsPayload = new java.util.HashMap<>();
            wsPayload.put("type", "MANUAL_ALERT");
            wsPayload.put("title", "Cập nhật cứu hộ");
            wsPayload.put("content", "Đội cứu hộ cập nhật trạng thái: " + request.getMessage());
            if (sos.getSender() != null) {
                wsPayload.put("targetUser", sos.getSender().getUserId());
            }
            wsPayload.put("targetSosId", sos.getId());
            wsPayload.put("targetPhone", sos.getSenderPhone());
            notificationSender.sendSystemNotification("/topic/alerts", wsPayload);
        } catch (Exception e) {
            log.error("Lỗi gửi WS cảnh báo cứu hộ: " + e.getMessage());
        }
    }
}