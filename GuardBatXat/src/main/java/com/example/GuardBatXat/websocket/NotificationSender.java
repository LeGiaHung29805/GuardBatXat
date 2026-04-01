package com.example.GuardBatXat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    // Đây là class cốt lõi của Spring Boot hỗ trợ gửi tin nhắn qua giao thức STOMP/WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Hàm gửi tín hiệu SOS khẩn cấp tới màn hình của Chỉ huy / Đội cứu hộ
     * * @param destination Kênh nhận dữ liệu (Ví dụ: "/topic/emergency")
     * @param payload     Dữ liệu mang theo (Ví dụ: Đối tượng SosRequest chứa Tọa độ, SĐT)
     */
    public void sendEmergencyAlert(String destination, Object payload) {
        log.info("Chuẩn bị phát tín hiệu cảnh báo tới kênh: {}", destination);

        try {
            // Hàm convertAndSend sẽ tự động biến Object (payload) thành chuỗi JSON
            // và đẩy thẳng xuống tất cả các Client đang lắng nghe ở destination này.
            messagingTemplate.convertAndSend(destination, payload);

            log.info("Phát tín hiệu Real-time thành công!");
        } catch (Exception e) {
            log.error("CÓ LỖI XẢY RA KHI PHÁT TÍN HIỆU WEBSOCKET: {}", e.getMessage());
        }
    }

    /**
     * Hàm dùng chung để gửi các thông báo hệ thống khác (Ví dụ: Thông báo mức nước lũ thay đổi)
     */
    public void sendSystemNotification(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}