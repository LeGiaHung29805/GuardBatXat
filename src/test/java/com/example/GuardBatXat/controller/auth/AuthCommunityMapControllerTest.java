package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.service.MapBroadcastService;
import com.example.GuardBatXat.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

class AuthCommunityMapControllerTest {

    private AuthCommunityMapController controller;
    private MapBroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        RiskService riskService = Mockito.mock(RiskService.class);
        broadcastService = Mockito.mock(MapBroadcastService.class);
        controller = new AuthCommunityMapController(riskService);
        ReflectionTestUtils.setField(controller, "broadcastService", broadcastService);
        ReflectionTestUtils.setField(controller, "internalApiToken", "internal-test-token");
    }

    @Test
    void triggerBroadcastRejectsMissingInternalToken() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.triggerFromPython(null)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void triggerBroadcastAcceptsMatchingInternalToken() {
        controller.triggerFromPython("internal-test-token");

        verify(broadcastService).broadcastNewLandslideHeatmap();
    }
}
