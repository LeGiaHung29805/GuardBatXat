package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.admin.DemoLocationAssignmentRequest;
import com.example.GuardBatXat.dto.response.auth.DemoLocationResponse;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.exception.ErrorCode;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.repository.projection.BuildingLocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoLocationService {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;

    @Transactional(readOnly = true)
    public DemoLocationResponse getForIdentifier(String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateCitizen(user);
        return getAssignedLocation(user);
    }

    @Transactional(readOnly = true)
    public DemoLocationResponse getForUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateCitizen(user);
        return getAssignedLocation(user);
    }

    @Transactional
    @CacheEvict(value = {"users", "userProfile", "survivalProfile"}, allEntries = true)
    public DemoLocationResponse assignToNearestBuilding(
            Integer userId,
            DemoLocationAssignmentRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateCitizen(user);

        BuildingLocationView nearestLocation = buildingRepository.findNearestLocation(
                        request.getLongitude(),
                        request.getLatitude()
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.RECORD_NOT_FOUND,
                        "Không tìm thấy ngôi nhà gần tọa độ đã nhập"
                ));

        user.setDefaultBuilding(
                buildingRepository.getReferenceById(nearestLocation.getBuildingId())
        );
        userRepository.save(user);
        return toResponse(nearestLocation);
    }

    private DemoLocationResponse getAssignedLocation(User user) {
        if (user.getDefaultBuilding() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Tài khoản chưa được gán ngôi nhà trình diễn"
            );
        }
        return locationForBuilding(user.getDefaultBuilding().getId());
    }

    private DemoLocationResponse locationForBuilding(Long buildingId) {
        BuildingLocationView location = buildingRepository.findLocationById(buildingId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RECORD_NOT_FOUND,
                        "Ngôi nhà chưa có dữ liệu tọa độ hợp lệ"
                ));

        return toResponse(location);
    }

    private DemoLocationResponse toResponse(BuildingLocationView location) {
        if (location.getBuildingId() == null
                || location.getLatitude() == null
                || location.getLongitude() == null
                || !Double.isFinite(location.getLatitude())
                || !Double.isFinite(location.getLongitude())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Ngôi nhà chưa có dữ liệu tọa độ hợp lệ"
            );
        }

        return DemoLocationResponse.builder()
                .buildingId(location.getBuildingId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .source("DEMO_HOME")
                .build();
    }

    private void validateCitizen(User user) {
        boolean active = Boolean.TRUE.equals(user.getIsActive());
        boolean citizen = user.getRole() != null
                && "CITIZEN".equals(user.getRole().getRoleName());
        if (!active || !citizen) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Vị trí trình diễn chỉ áp dụng cho tài khoản Citizen đang hoạt động"
            );
        }
    }
}
