package com.example.GuardBatXat.service;

import com.example.GuardBatXat.event.AhpWeightsUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiRoutingRebuildNotifierTest {

    @Test
    void requestsProtectedRebuildAfterCommittedWeightChange() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiRoutingRebuildNotifier notifier = new AiRoutingRebuildNotifier(restTemplate);
        ReflectionTestUtils.setField(notifier, "aiServiceBaseUrl", "http://ai:5000");
        ReflectionTestUtils.setField(notifier, "internalApiToken", "shared-token");
        when(restTemplate.exchange(
                eq("http://ai:5000/api/v1/ai/internal/rebuild-routing"),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());

        notifier.requestRebuild(new AhpWeightsUpdatedEvent("safety"));

        verify(restTemplate).exchange(
                eq("http://ai:5000/api/v1/ai/internal/rebuild-routing"),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)
        );
    }

    @Test
    void skipsRebuildWhenInternalTokenIsNotConfigured() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        AiRoutingRebuildNotifier notifier = new AiRoutingRebuildNotifier(restTemplate);
        ReflectionTestUtils.setField(notifier, "internalApiToken", "");

        notifier.requestRebuild(new AhpWeightsUpdatedEvent("rescue"));

        verify(restTemplate, never()).exchange(any(), any(), any(), eq(Void.class));
    }
}
