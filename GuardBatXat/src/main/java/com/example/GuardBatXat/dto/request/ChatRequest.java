package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "ID/Số điện thoại SOS không được để trống")
    private String sosId;

    @NotBlank(message = "Người gửi không được để trống")
    private String sender; // Ví dụ: "Nạn nhân" hoặc "Đội cứu hộ"

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String message;
}
