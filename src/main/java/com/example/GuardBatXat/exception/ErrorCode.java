package com.example.GuardBatXat.exception;

public enum ErrorCode {
    USER_NOT_FOUND(404, "Không tìm thấy người dùng này trong hệ thống"),
    USER_EXISTED(400, "Tên đăng nhập đã tồn tại"),
    UNAUTHORIZED(401, "Bạn chưa đăng nhập hoặc thẻ đã hết hạn"),
    SYSTEM_ERROR(500, "Lỗi hệ thống máy chủ, vui lòng thử lại sau"),
    INVALID_DATA(400, "Dữ liệu đầu vào không hợp lệ"),
    RECORD_NOT_FOUND(404, "Không tìm thấy bản ghi này"),
    MISSION_NOT_FOUND(404, "Không tìm thấy mission cứu hộ này"),
    INVALID_REQUEST(400, "Yêu cầu không hợp lệ");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}