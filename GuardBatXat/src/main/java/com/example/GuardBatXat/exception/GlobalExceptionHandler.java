package com.example.GuardBatXat.exception;

import com.example.GuardBatXat.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Bắt lỗi Custom của dự án (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    // 2. Bắt lỗi người dùng nhập thiếu dữ liệu (@Valid, @NotNull, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        // Lấy thông báo lỗi đầu tiên mà ta đã cài đặt trong DTO (VD: "Số điện thoại không được để trống")
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(ErrorCode.INVALID_DATA.getCode());
        response.setMessage(errorMessage);

        return ResponseEntity.badRequest().body(response);
    }

    // 3. Bắt mọi lỗi hệ thống (Chống sập App)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        logger.error("Lỗi hệ thống nghiêm trọng: ", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.<String>builder()
                        .code(500)
                        .message("Lỗi: " + ex.getMessage() + " | " + ex.getClass().getName())
                        .build());
    }
}