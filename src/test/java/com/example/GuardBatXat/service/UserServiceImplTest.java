package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.auth.UserCreationRequest;
import com.example.GuardBatXat.dto.request.admin.AdminUserCreateRequest;
import com.example.GuardBatXat.dto.request.admin.AdminUserUpdateRequest;
import com.example.GuardBatXat.dto.response.auth.UserResponse;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.mapper.UserMapper;
import com.example.GuardBatXat.mapper.UserProfileMapper;
import com.example.GuardBatXat.repository.BuildingRepository;
import com.example.GuardBatXat.repository.RoleRepository;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.repository.WeatherStationRepository;
import com.example.GuardBatXat.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private BuildingRepository buildingRepository;
    @Mock private WeatherStationRepository weatherStationRepository;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                userMapper,
                userProfileMapper,
                buildingRepository,
                weatherStationRepository
        );
    }

    @Test
    void publicRegistrationAlwaysCreatesCitizen() {
        Role citizen = new Role(1, "CITIZEN", null);
        UserCreationRequest request = UserCreationRequest.builder()
                .username("ignored")
                .emailOrPhone("new.user@example.com")
                .password("secret123")
                .fullName("New User")
                .roleName("ADMIN")
                .assignedStation("ST01")
                .build();

        when(roleRepository.findByRoleName("CITIZEN")).thenReturn(Optional.of(citizen));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        service.registerCitizen(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("CITIZEN", userCaptor.getValue().getRole().getRoleName());
        assertEquals(null, userCaptor.getValue().getAssignedStation());
    }

    @Test
    void adminCreationRejectsLegacyViewerRole() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("ignored")
                .emailOrPhone("new.user@example.com")
                .password("secret123")
                .roleName("VIEWER")
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminCreationPersistsTheDedicatedAdminPayload() {
        Role rescueTeam = new Role(2, "RESCUE_TEAM", null);
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("rescue01");
        request.setPassword("secret123");
        request.setFullName("Đội cứu hộ 01");
        request.setEmail("rescue01@example.com");
        request.setPhoneNumber("0912345678");
        request.setRoleName("RESCUE_TEAM");
        request.setAssignedStation("ST01");

        when(roleRepository.findByRoleName("RESCUE_TEAM")).thenReturn(Optional.of(rescueTeam));
        when(weatherStationRepository.existsById("ST01")).thenReturn(true);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        service.createAdminUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("rescue01", savedUser.getUsername());
        assertEquals("rescue01@example.com", savedUser.getEmail());
        assertEquals("0912345678", savedUser.getPhoneNumber());
        assertEquals("ST01", savedUser.getAssignedStation());
        assertEquals("RESCUE_TEAM", savedUser.getRole().getRoleName());
    }

    @Test
    void adminUpdateCanClearStationAndOptionalEmail() {
        Role rescueTeam = new Role(2, "RESCUE_TEAM", null);
        User user = new User();
        user.setUserId(10);
        user.setFullName("Tên cũ");
        user.setEmail("old@example.com");
        user.setPhoneNumber("0911111111");
        user.setAssignedStation("ST01");
        user.setRole(rescueTeam);

        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setFullName("Tên mới");
        request.setEmail("");
        request.setPhoneNumber("0922222222");
        request.setRoleName("RESCUE_TEAM");
        request.setAssignedStation(null);

        when(userRepository.findById(10)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName("RESCUE_TEAM")).thenReturn(Optional.of(rescueTeam));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        service.updateAdminUser(10, request);

        assertEquals("Tên mới", user.getFullName());
        assertEquals(null, user.getEmail());
        assertEquals("0922222222", user.getPhoneNumber());
        assertEquals(null, user.getAssignedStation());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void cannotDeleteLastAdmin() {
        Role admin = new Role(3, "ADMIN", null);
        User user = new User();
        user.setUserId(10);
        user.setRole(admin);

        when(userRepository.findById(10)).thenReturn(Optional.of(user));
        when(userRepository.countByRoleRoleName("ADMIN")).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.deleteUser(10));
        verify(userRepository, never()).delete(any(User.class));
    }
}
