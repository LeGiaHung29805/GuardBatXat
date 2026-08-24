package com.example.GuardBatXat.exception;

import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Bắt lỗi Custom của dự án (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage());

        // Giữ HTTP status đồng bộ với mã lỗi nghiệp vụ (400, 401, 404, 500).
        return ResponseEntity.status(errorCode.getCode()).body(response);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .code(400)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.<Void>builder()
                .code(409)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        logger.warn("Database constraint rejected a request", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.<Void>builder()
                .code(409)
                .message("Dữ liệu xung đột với ràng buộc hiện có")
                .build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(ex.getStatusCode().value())
                .message(ex.getReason())
                .build());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(ErrorCode.INVALID_DATA.getCode());
        response.setMessage("Tọa độ hoặc định dạng yêu cầu không hợp lệ");
        return ResponseEntity.badRequest().body(response);
    }

    // 3. Bắt mọi lỗi hệ thống (Chống sập App)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        logger.error("Lỗi hệ thống nghiêm trọng: ", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.<String>builder()
                        .code(500)
                        .message("Hệ thống gặp lỗi nội bộ. Vui lòng thử lại sau")
                        .build());
    }
}
