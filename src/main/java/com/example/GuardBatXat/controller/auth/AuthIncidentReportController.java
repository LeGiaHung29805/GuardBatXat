package com.example.GuardBatXat.controller.auth;

import com.example.GuardBatXat.dto.request.rescue.IncidentReportRequest;
import com.example.GuardBatXat.dto.response.rescue.ApiResponse;
import com.example.GuardBatXat.dto.response.rescue.IncidentReportResponse;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.service.IncidentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthIncidentReportController {

    private final IncidentReportService incidentReportService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentReportResponse>> createReport(@RequestBody IncidentReportRequest request) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = null;
        if (identifier != null && !identifier.isEmpty() && !"anonymousUser".equals(identifier)) {
            currentUser = userRepository.findByIdentifier(identifier).orElse(null);
        }

        IncidentReportResponse data = incidentReportService.createReport(request, currentUser);
        return ResponseEntity.ok(ApiResponse.<IncidentReportResponse>builder()
                .code(200)
                .message("Báo cáo sự cố đã được gửi và đang chờ kiểm duyệt từ Ban chỉ huy.")
                .data(data)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentReportResponse>>> getAllReports(
            @RequestParam(value = "status", required = false) String status) {
        List<IncidentReportResponse> data;
        if (status != null && !status.isEmpty()) {
            data = incidentReportService.getReportsByStatus(status);
        } else {
            data = incidentReportService.getAllReports();
        }
        return ResponseEntity.ok(ApiResponse.<List<IncidentReportResponse>>builder()
                .code(200)
                .message("Lấy danh sách báo cáo thành công")
                .data(data)
                .build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") String status) {
        IncidentReportResponse data = incidentReportService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<IncidentReportResponse>builder()
                .code(200)
                .message("Cập nhật trạng thái sự cố thành công")
                .data(data)
                .build());
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<IncidentReportResponse>>> getMyReports() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = null;
        if (identifier != null && !identifier.isEmpty() && !"anonymousUser".equals(identifier)) {
            currentUser = userRepository.findByIdentifier(identifier).orElse(null);
        }

        List<IncidentReportResponse> data = incidentReportService.getMyReports(currentUser);
        return ResponseEntity.ok(ApiResponse.<List<IncidentReportResponse>>builder()
                .code(200)
                .message("Lấy lịch sử báo cáo của tôi thành công")
                .data(data)
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getIncidentStats() {
        Map<String, Long> data = incidentReportService.getStats();
        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder()
                .code(200)
                .message("Lấy thống kê báo cáo sự cố thành công")
                .data(data)
                .build());
    }
}
