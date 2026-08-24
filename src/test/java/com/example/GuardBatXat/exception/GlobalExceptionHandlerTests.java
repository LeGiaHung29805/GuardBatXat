package com.example.GuardBatXat.exception;

import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void keepsHttpStatusAlignedWithApplicationErrorCode() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(
                new AppException(ErrorCode.UNAUTHORIZED)
        );

        assertEquals(401, response.getStatusCode().value());
        assertEquals(401, response.getBody().getCode());
    }
}
