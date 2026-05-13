package com.example.GuardBatXat.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SosUpdateLogResponse {
    private Integer id;
    private Integer missionId;
    private String status;
    private String message;
    private Double lat;
    private Double lng;
    private List<String> images;
    private LocalDateTime timestamp;
}
