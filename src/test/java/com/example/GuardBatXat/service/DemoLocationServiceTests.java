package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.admin.DemoLocationAssignmentRequest;
import com.example.GuardBatXat.dto.response.auth.DemoLocationResponse;
import com.example.GuardBatXat.entity.Building;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.repository.projection.BuildingLocationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoLocationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BuildingRepository buildingRepository;

    private DemoLocationService demoLocationService;

    @BeforeEach
    void setUp() {
        demoLocationService = new DemoLocationService(userRepository, buildingRepository);
    }

    @Test
    void assignsNearestBuildingAndReturnsPointInsideIt() {
        User citizen = activeCitizen();
        Building building = new Building();
        building.setId(501L);
        DemoLocationAssignmentRequest request = request(22.5458, 103.8895);

        when(userRepository.findById(citizen.getUserId())).thenReturn(Optional.of(citizen));
        when(buildingRepository.findNearestLocation(103.8895, 22.5458))
                .thenReturn(Optional.of(location(501L, 22.54581, 103.88951)));
        when(buildingRepository.getReferenceById(501L)).thenReturn(building);

        DemoLocationResponse response = demoLocationService.assignToNearestBuilding(
                citizen.getUserId(),
                request
        );

        assertSame(building, citizen.getDefaultBuilding());
        assertEquals(501L, response.getBuildingId());
        assertEquals(22.54581, response.getLatitude());
        assertEquals(103.88951, response.getLongitude());
        assertEquals("DEMO_HOME", response.getSource());
        verify(userRepository).save(citizen);
    }

    @Test
    void rejectsCitizenWithoutAssignedBuilding() {
        User citizen = activeCitizen();
        when(userRepository.findById(citizen.getUserId())).thenReturn(Optional.of(citizen));

        assertThrows(
                AppException.class,
                () -> demoLocationService.getForUserId(citizen.getUserId())
        );
    }

    @Test
    void rejectsPrivilegedAccount() {
        User admin = activeCitizen();
        admin.getRole().setRoleName("ADMIN");
        when(userRepository.findById(admin.getUserId())).thenReturn(Optional.of(admin));

        assertThrows(
                AppException.class,
                () -> demoLocationService.assignToNearestBuilding(
                        admin.getUserId(),
                        request(22.5458, 103.8895)
                )
        );
    }

    private User activeCitizen() {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("CITIZEN");

        User user = new User();
        user.setUserId(101);
        user.setUsername("teacher01@batxat.local");
        user.setIsActive(true);
        user.setRole(role);
        return user;
    }

    private DemoLocationAssignmentRequest request(double latitude, double longitude) {
        DemoLocationAssignmentRequest request = new DemoLocationAssignmentRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        return request;
    }

    private BuildingLocationView location(long buildingId, double latitude, double longitude) {
        return new BuildingLocationView() {
            @Override
            public Long getBuildingId() {
                return buildingId;
            }

            @Override
            public Double getLatitude() {
                return latitude;
            }

            @Override
            public Double getLongitude() {
                return longitude;
            }
        };
    }
}
