package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.SosRequest;
import com.example.GuardBatXat.repository.SosRequestRepository;
import com.example.GuardBatXat.service.SosService;
import com.example.GuardBatXat.websocket.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosServiceImpl implements SosService {

    private final SosRequestRepository sosRequestRepository;
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
}