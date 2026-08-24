package com.example.GuardBatXat.service;

import com.example.GuardBatXat.entity.ModelRegistry;
import com.example.GuardBatXat.entity.AhpWeight;
import com.example.GuardBatXat.dto.request.admin.AhpWeightRequest;
import com.example.GuardBatXat.repository.AhpWeightRepository;
import com.example.GuardBatXat.repository.ModelRegistryRepository;
import com.example.GuardBatXat.service.impl.AdminSystemConfigServiceImpl;
import com.example.GuardBatXat.websocket.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSystemConfigServiceImplTest {

    @Mock private ModelRegistryRepository modelRegistryRepository;
    @Mock private AhpWeightRepository ahpWeightRepository;
    @Mock private NotificationSender notificationSender;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AdminSystemConfigServiceImpl service;
    private ModelRegistry floodModel;

    @BeforeEach
    void setUp() {
        service = new AdminSystemConfigServiceImpl(
                modelRegistryRepository,
                ahpWeightRepository,
                notificationSender,
                restTemplate,
                eventPublisher
        );
        ReflectionTestUtils.setField(service, "aiServiceBaseUrl", "http://localhost:5000");

        floodModel = new ModelRegistry();
        floodModel.setId(67);
        floodModel.setModelName("LSTM_Flood_20260803_2320");
        floodModel.setModelTarget("FLOOD");
        floodModel.setModelPath("models\\lstm_flood_v1_20260803_2320.keras");
        floodModel.setScalerPath("models\\scaler_lstm_v1_20260803_2320.pkl");
        floodModel.setIsActive(false);
    }

    @Test
    void validatesArtifactBeforeChangingActiveModel() {
        when(modelRegistryRepository.findById(67)).thenReturn(Optional.of(floodModel));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new IllegalStateException("invalid artifact"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.activateModel(67)
        );

        assertEquals(422, error.getStatusCode().value());
        verify(modelRegistryRepository, never()).deactivateAllModelsByTarget(anyString());
        verify(modelRegistryRepository, never()).save(any(ModelRegistry.class));
    }

    @Test
    void activatesModelAfterAiServiceAcceptsContract() {
        when(modelRegistryRepository.findById(67)).thenReturn(Optional.of(floodModel));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("valid", true));
        when(modelRegistryRepository.save(floodModel)).thenReturn(floodModel);

        service.activateModel(67);

        verify(modelRegistryRepository).deactivateAllModelsByTarget("FLOOD");
        verify(modelRegistryRepository).save(floodModel);
        assertEquals(true, floodModel.getIsActive());
    }

    @Test
    void persistsExactDecimalAhpWeights() {
        AhpWeight weight = new AhpWeight();
        weight.setStrategyName("safety");
        AhpWeightRequest request = AhpWeightRequest.builder()
                .wDistance(new BigDecimal("0.20000"))
                .wFlood(new BigDecimal("0.20000"))
                .wLandslide(new BigDecimal("0.20000"))
                .wCapacity(new BigDecimal("0.15000"))
                .wBridge(new BigDecimal("0.10000"))
                .wReport(new BigDecimal("0.15000"))
                .build();

        when(ahpWeightRepository.findById("safety")).thenReturn(Optional.of(weight));
        when(ahpWeightRepository.save(weight)).thenReturn(weight);

        service.updateAhpWeights("safety", request);

        assertEquals(new BigDecimal("0.20000"), weight.getWDistance());
        assertEquals(new BigDecimal("0.15000"), weight.getWReport());
        verify(eventPublisher).publishEvent(any(com.example.GuardBatXat.event.AhpWeightsUpdatedEvent.class));
    }
}
