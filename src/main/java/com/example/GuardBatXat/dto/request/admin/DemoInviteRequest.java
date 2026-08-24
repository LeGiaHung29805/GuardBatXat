package com.example.GuardBatXat.dto.request.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemoInviteRequest {

    @NotBlank(message = "Tài khoản Citizen không được để trống")
    private String identifier;

    @Min(value = 5, message = "Thời hạn QR tối thiểu là 5 phút")
    @Max(value = 1440, message = "Thời hạn QR tối đa là 1440 phút")
    private Integer ttlMinutes = 120;
}
