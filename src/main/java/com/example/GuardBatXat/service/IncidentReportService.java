package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.rescue.IncidentReportRequest;
import com.example.GuardBatXat.dto.response.rescue.IncidentReportResponse;
import com.example.GuardBatXat.entity.User;
import java.util.List;
import java.util.Map;

public interface IncidentReportService {
    IncidentReportResponse createReport(IncidentReportRequest request, User currentUser);
    List<IncidentReportResponse> getReportsByStatus(String status);
    IncidentReportResponse updateStatus(Integer reportId, String status);
    List<IncidentReportResponse> getAllReports();
    List<IncidentReportResponse> getMyReports(User user);
    Map<String, Long> getStats();
}
