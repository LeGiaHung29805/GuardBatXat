package com.example.GuardBatXat.service;

import com.example.GuardBatXat.event.AhpWeightsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiRoutingRebuildNotifier {

    private final RestTemplate restTemplate;

    @Value("${batxat.ai.service.base-url:http://localhost:5000}")
    private String aiServiceBaseUrl;

    @Value("${batxat.security.internal-api-token:}")
    private String internalApiToken;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requestRebuild(AhpWeightsUpdatedEvent event) {
        if (internalApiToken == null || internalApiToken.isBlank()) {
            log.warn("AHP updated for {}, but INTERNAL_API_TOKEN is not configured; routing rebuild skipped", event.strategyName());
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Token", internalApiToken);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    aiServiceBaseUrl + "/api/v1/ai/internal/rebuild-routing",
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("AI routing rebuild request for {} returned {}", event.strategyName(), response.getStatusCode());
            }
        } catch (Exception error) {
            log.warn("Could not request AI routing rebuild for {}: {}", event.strategyName(), error.getMessage());
        }
    }
}
