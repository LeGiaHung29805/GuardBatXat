package com.example.GuardBatXat.dto.response.commander;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloodStatisticDto {
    private String riskStatus;       // Trạng thái (VD: Nguy cơ Cao, Trung bình)
    private Long buildingCount;      // Số lượng nhà bị ngập
    private Double totalArea;        // Tổng diện tích bị ảnh hưởng (m2)
    private Long totalCapacity;      // Tổng số người cần sơ tán
}