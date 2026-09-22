package com.example.GuardBatXat.service;

import com.example.GuardBatXat.websocket.NotificationSender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskOutboxPublisher {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationSender notificationSender;

    @Scheduled(fixedDelayString = "${batxat.risk.outbox-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<Map<String, Object>> events = jdbcTemplate.queryForList("""
                SELECT event_id, destination, payload
                FROM batxat_risk_event_outbox
                WHERE status='PENDING' AND available_at<=NOW()
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED LIMIT 20
                """);
        for (Map<String, Object> event : events) {
            UUID eventId = (UUID) event.get("event_id");
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        String.valueOf(event.get("payload")), new TypeReference<>() {});
                notificationSender.sendSystemNotification(
                        String.valueOf(event.get("destination")), payload);
                jdbcTemplate.update("""
                        UPDATE batxat_risk_event_outbox
                        SET status='PUBLISHED', attempts=attempts+1,
                            published_at=NOW(), last_error=NULL
                        WHERE event_id=?
                        """, eventId);
            } catch (Exception exception) {
                log.error("Không phát được risk event {}", eventId, exception);
                jdbcTemplate.update("""
                        UPDATE batxat_risk_event_outbox
                        SET attempts=attempts+1, last_error=?,
                            available_at=NOW() + INTERVAL '10 seconds'
                        WHERE event_id=?
                        """, exception.getMessage(), eventId);
            }
        }
    }
}
