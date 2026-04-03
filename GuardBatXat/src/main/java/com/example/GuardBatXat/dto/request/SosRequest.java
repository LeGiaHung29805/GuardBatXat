package com.example.GuardBatXat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    private String senderPhone; // Đã khớp với tên leader

    @NotBlank(message = "Nội dung kêu cứu không được để trống")
    private String message;

    @NotNull(message = "Vĩ độ (lat) không được để trống")
    private Double lat;

    @NotNull(message = "Kinh độ (lng) không được để trống")
    private Double lng;
}