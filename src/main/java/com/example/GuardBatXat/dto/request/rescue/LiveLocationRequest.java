package com.example.GuardBatXat.dto.request.rescue;

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
public class LiveLocationRequest {

    @NotBlank(message = "ID/Số điện thoại không được để trống")
    private String entityId;

    @NotNull(message = "Vĩ độ không được để trống")
    private Double lat;

    @NotNull(message = "Kinh độ không được để trống")
    private Double lng;

    private String role; // Ví dụ: "VICTIM" hoặc "RESCUE_TEAM"
}
