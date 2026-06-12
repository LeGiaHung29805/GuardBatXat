package com.example.GuardBatXat.dto.request.rescue;
import com.example.GuardBatXat.entity.SosUpdateLog;

import lombok.Data;
import java.util.List;

@Data
public class SosUpdateLogRequest {
    private String status;
    private String message;
    private Double lat;
    private Double lng;
    private List<String> images;
}
