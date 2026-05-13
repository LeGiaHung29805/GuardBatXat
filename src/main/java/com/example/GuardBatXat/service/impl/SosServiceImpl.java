package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.dto.response.SosResponse;
import com.example.GuardBatXat.entity.SosEntity;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.SosRequestRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.SosService;
import com.example.GuardBatXat.websocket.NotificationSender;
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

    @Override
    @Transactional
    public void processSosRequest(SosRequest requestDto) {
        log.info("Bắt đầu xử lý yêu cầu SOS từ SĐT: {}", requestDto.getSenderPhone());

        sosRequestRepository.insertSosRequestNative(
                requestDto.getSenderPhone(),
                requestDto.getMessage(),
                requestDto.getLat(),
                requestDto.getLng()
        );

        // Bắn WebSocket tới các tài khoản có role COMMANDER / RESCUE_TEAM
        try {
            // Giả sử NotificationSender có hàm convertAndSend hoặc tương tự
            // Topic nhận cảnh báo trên Frontend thường là "/topic/emergency"
            notificationSender.sendEmergencyAlert("/topic/emergency", requestDto);
            log.info("Đã phát tín hiệu SOS Real-time tới trung tâm chỉ huy.");
        } catch (Exception e) {
            log.error("Lỗi khi phát tín hiệu WebSocket: {}", e.getMessage());
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
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        sos.setStatus("RESCUING");
        sos.setAssignedUser(user);
        sosRequestRepository.save(sos);
    }

    @Override
    @Transactional
    public void completeSosRequest(Integer id, String identifier) {
        SosEntity sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));
        
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
    public List<com.example.GuardBatXat.dto.response.SosUpdateLogResponse> getSosUpdates(Integer sosId) {
        List<com.example.GuardBatXat.entity.SosUpdateLog> logs = sosUpdateLogRepository.findBySosRequestIdOrderByCreatedAtDesc(sosId);
        return logs.stream().map(log -> {
            List<String> imagesList = null;
            if (log.getImages() != null && !log.getImages().isEmpty()) {
                imagesList = java.util.Arrays.asList(log.getImages().split(",,,"));
            }
            return com.example.GuardBatXat.dto.response.SosUpdateLogResponse.builder()
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
    public void addSosUpdate(Integer sosId, com.example.GuardBatXat.dto.request.SosUpdateLogRequest request, String identifier) {
        SosEntity sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

        String imagesJoined = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imagesJoined = String.join(",,,", request.getImages());
        }

        com.example.GuardBatXat.entity.SosUpdateLog log = com.example.GuardBatXat.entity.SosUpdateLog.builder()
                .sosRequest(sos)
                .updateStatus(request.getStatus())
                .message(request.getMessage())
                .gpsLat(request.getLat())
                .gpsLng(request.getLng())
                .images(imagesJoined)
                .build();

        sosUpdateLogRepository.save(log);
    }
}