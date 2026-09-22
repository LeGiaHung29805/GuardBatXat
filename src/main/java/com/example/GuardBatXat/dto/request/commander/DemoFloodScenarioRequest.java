package com.example.GuardBatXat.dto.request.commander;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemoFloodScenarioRequest {

    @NotBlank(message = "Tên kịch bản không được để trống")
    private String scenarioName = "Kịch bản mưa tùy chỉnh";

    private String baselineDate;

    @DecimalMin(value = "0.0", message = "Lượng mưa không được âm")
    @DecimalMax(value = "500.0", message = "Lượng mưa demo tối đa là 500 mm/ngày")
    private Double uniformRainMm;

    @DecimalMin("0.0") @DecimalMax("500.0")
    private Double rainYTyMm;

    @DecimalMin("0.0") @DecimalMax("500.0")
    private Double rainSangMaSaoMm;

    @DecimalMin("0.0") @DecimalMax("500.0")
    private Double rainTrinhTuongMm;

    @DecimalMin("0.0") @DecimalMax("500.0")
    private Double rainBatXatMm;

    @DecimalMin(value = "0.0", message = "Độ bão hòa đất tối thiểu là 0%")
    @DecimalMax(value = "100.0", message = "Độ bão hòa đất tối đa là 100%")
    private Double soilSaturationPct = 70.0;

    @DecimalMin(value = "1.0", message = "Thời gian mưa tối thiểu là 1 giờ")
    @DecimalMax(value = "168.0", message = "Thời gian mưa tối đa là 168 giờ")
    private Double durationHours = 6.0;

    @DecimalMin(value = "-5.0", message = "Điều chỉnh nước tối thiểu là -5 m")
    @DecimalMax(value = "10.0", message = "Điều chỉnh nước tối đa là 10 m")
    private Double manualWaterRiseM = 0.0;

    private String simulationNote;

    @AssertTrue(message = "Hãy nhập mưa toàn huyện hoặc ít nhất một trạm")
    public boolean hasRainInput() {
        return uniformRainMm != null
                || rainYTyMm != null
                || rainSangMaSaoMm != null
                || rainTrinhTuongMm != null
                || rainBatXatMm != null;
    }
}

