package com.example.GuardBatXat.dto.response.rescue;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReportResponse {
    private Integer id;
    private String reporterName;
    private String reporterPhone;
    private String incidentType;
    private String impactLevel;
    private String description;
    private List<String> images;
    private Double gpsLat;
    private Double gpsLng;
    private String status;
    private LocalDateTime createdAt;
}
