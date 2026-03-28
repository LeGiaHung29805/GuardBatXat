//package com.example.GuardBatXat.exception;
//
//import com.example.GuardBatXat.dto.response.ApiResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
//
//    @ExceptionHandler(AppException.class)
//    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
//        ErrorCode errorCode = ex.getErrorCode();
//        ApiResponse<Void> response = new ApiResponse<>();
//        response.setCode(errorCode.getCode());
//        response.setMessage(errorCode.getMessage());
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
//        logger.error("Lỗi hệ thống nghiêm trọng: ", ex);
//
//        ApiResponse<Void> response = new ApiResponse<>();
//        response.setCode(ErrorCode.SYSTEM_ERROR.getCode());
//        response.setMessage(ErrorCode.SYSTEM_ERROR.getMessage());
//
//        return ResponseEntity.internalServerError().body(response);
//    }
//}