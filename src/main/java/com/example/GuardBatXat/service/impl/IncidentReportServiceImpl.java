package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.dto.request.rescue.IncidentReportRequest;
import com.example.GuardBatXat.dto.response.rescue.IncidentReportResponse;
import com.example.GuardBatXat.entity.IncidentReport;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.IncidentReportRepository;
import com.example.GuardBatXat.repository.RoadEdgeRepository;
import com.example.GuardBatXat.service.IncidentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentReportServiceImpl implements IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final RoadEdgeRepository roadEdgeRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public IncidentReportResponse createReport(IncidentReportRequest request, User currentUser) {
        log.info("Tạo báo cáo sự cố mới: {}", request.getIncidentType());

        String imagesJoined = null;
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imagesJoined = String.join(",,,", request.getImages());
        }

        Point geom = geometryFactory.createPoint(new Coordinate(request.getGpsLng(), request.getGpsLat()));

        IncidentReport report = IncidentReport.builder()
                .reporter(currentUser)
                .reporterName(currentUser != null ? currentUser.getFullName() : request.getReporterName())
                .reporterPhone(currentUser != null ? currentUser.getPhoneNumber() : request.getReporterPhone())
                .incidentType(request.getIncidentType())
                .impactLevel(request.getImpactLevel())
                .description(request.getDescription())
                .images(imagesJoined)
                .gpsLat(request.getGpsLat())
                .gpsLng(request.getGpsLng())
                .geom(geom)
                .status("PENDING")
                .build();

        IncidentReport saved = incidentReportRepository.save(report);
        return mapToResponse(saved);
    }

    @Override
    public List<IncidentReportResponse> getReportsByStatus(String status) {
        List<IncidentReport> reports = incidentReportRepository.findByStatus(status);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncidentReportResponse updateStatus(Integer reportId, String status) {
        log.info("Cập nhật trạng thái báo cáo sự cố #{} sang {}", reportId, status);
        IncidentReport report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo sự cố"));

        String oldStatus = report.getStatus();
        report.setStatus(status);
        IncidentReport updated = incidentReportRepository.save(report);

        // Nếu được duyệt và trước đó chưa được duyệt, cập nhật cản trở đường bộ
        if ("APPROVED".equalsIgnoreCase(status) && !"APPROVED".equalsIgnoreCase(oldStatus)) {
            log.info("Báo cáo được DUYỆT. Tăng community_report cho đoạn đường lân cận [{}, {}]", report.getGpsLat(), report.getGpsLng());
            // 0.0002 độ tương đương khoảng 20-25m trong thực tế
            roadEdgeRepository.incrementCommunityReportNear(report.getGpsLat(), report.getGpsLng(), 0.0002);
        }

        return mapToResponse(updated);
    }

    @Override
    public List<IncidentReportResponse> getAllReports() {
        List<IncidentReport> reports = incidentReportRepository.findAll();
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private IncidentReportResponse mapToResponse(IncidentReport entity) {
        List<String> imagesList = Collections.emptyList();
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            imagesList = Arrays.asList(entity.getImages().split(",,,"));
        }

        return IncidentReportResponse.builder()
                .id(entity.getId())
                .reporterName(entity.getReporterName())
                .reporterPhone(entity.getReporterPhone())
                .incidentType(entity.getIncidentType())
                .impactLevel(entity.getImpactLevel())
                .description(entity.getDescription())
                .images(imagesList)
                .gpsLat(entity.getGpsLat())
                .gpsLng(entity.getGpsLng())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public List<IncidentReportResponse> getMyReports(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        List<IncidentReport> reports = incidentReportRepository.findByReporterOrderByCreatedAtDesc(user);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", incidentReportRepository.countByStatus("PENDING"));
        stats.put("approved", incidentReportRepository.countByStatus("APPROVED"));
        stats.put("resolved", incidentReportRepository.countByStatus("RESOLVED"));
        stats.put("total", incidentReportRepository.count());
        return stats;
    }
}
