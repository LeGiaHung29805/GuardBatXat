package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.admin.DemoInviteRequest;
import com.example.GuardBatXat.dto.response.admin.DemoInviteResponse;
import com.example.GuardBatXat.entity.Role;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.entity.Building;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoLoginServiceTests {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    private final Map<String, Object> redisValues = new ConcurrentHashMap<>();
    private DemoLoginService demoLoginService;

    @BeforeEach
    void setUp() {
        redisValues.clear();
        demoLoginService = new DemoLoginService(
                redisCacheService,
                userRepository,
                userDetailsService,
                jwtService
        );
    }

    @Test
    void tokenCanBeExchangedOnlyOnce() {
        stubRedis();
        User citizen = activeCitizen();
        when(userRepository.findByIdentifier(anyString())).thenReturn(Optional.of(citizen));
        when(userDetailsService.loadUserByUsername(eq(citizen.getUsername())))
                .thenReturn(userDetails);
        when(jwtService.generateDemoToken(userDetails)).thenReturn("demo-jwt");

        DemoInviteResponse invite = demoLoginService.createInvite(requestFor(citizen));

        assertEquals("demo-jwt", demoLoginService.exchangeToken(invite.getToken()));
        assertThrows(AppException.class, () -> demoLoginService.exchangeToken(invite.getToken()));
    }

    @Test
    void newerQrInvalidatesPreviousQrForSameCitizen() {
        stubRedis();
        User citizen = activeCitizen();
        when(userRepository.findByIdentifier(anyString())).thenReturn(Optional.of(citizen));
        when(userDetailsService.loadUserByUsername(eq(citizen.getUsername())))
                .thenReturn(userDetails);
        when(jwtService.generateDemoToken(userDetails)).thenReturn("demo-jwt");

        DemoInviteResponse first = demoLoginService.createInvite(requestFor(citizen));
        DemoInviteResponse second = demoLoginService.createInvite(requestFor(citizen));

        assertNotEquals(first.getToken(), second.getToken());
        assertThrows(AppException.class, () -> demoLoginService.exchangeToken(first.getToken()));
        assertEquals("demo-jwt", demoLoginService.exchangeToken(second.getToken()));
    }

    @Test
    void privilegedAccountCannotReceiveDemoQr() {
        User admin = activeCitizen();
        admin.getRole().setRoleName("ADMIN");
        when(userRepository.findByIdentifier(anyString())).thenReturn(Optional.of(admin));

        assertThrows(AppException.class, () -> demoLoginService.createInvite(requestFor(admin)));
    }

    private DemoInviteRequest requestFor(User user) {
        DemoInviteRequest request = new DemoInviteRequest();
        request.setIdentifier(user.getUsername());
        request.setTtlMinutes(120);
        return request;
    }

    private void stubRedis() {
        doAnswer(invocation -> {
            redisValues.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(redisCacheService).setCache(anyString(), org.mockito.ArgumentMatchers.any(), anyLong());

        when(redisCacheService.getCache(anyString()))
                .thenAnswer(invocation -> redisValues.get(invocation.getArgument(0)));
        when(redisCacheService.getAndDelete(anyString()))
                .thenAnswer(invocation -> redisValues.remove(invocation.getArgument(0)));
        doAnswer(invocation -> {
            redisValues.remove(invocation.getArgument(0));
            return null;
        }).when(redisCacheService).deleteCache(anyString());
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
        Building building = new Building();
        building.setId(501L);
        user.setDefaultBuilding(building);
        return user;
    }
}
