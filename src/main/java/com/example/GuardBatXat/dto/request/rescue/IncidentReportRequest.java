package com.example.GuardBatXat.dto.request.rescue;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReportRequest {
    private String reporterName;
    private String reporterPhone;
    private String incidentType;
    private String impactLevel;
    private String description;
    private List<String> images;
    private Double gpsLat;
    private Double gpsLng;
}
