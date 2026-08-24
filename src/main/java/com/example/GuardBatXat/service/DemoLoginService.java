package com.example.GuardBatXat.service;

import com.example.GuardBatXat.dto.request.admin.DemoInviteRequest;
import com.example.GuardBatXat.dto.response.admin.DemoInviteResponse;
import com.example.GuardBatXat.entity.User;
import com.example.GuardBatXat.exception.AppException;
import com.example.GuardBatXat.exception.ErrorCode;
import com.example.GuardBatXat.repository.UserRepository;
import com.example.GuardBatXat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class DemoLoginService {

    private static final String REDIS_KEY_PREFIX = "demo:login:";
    private static final String REDIS_USER_KEY_PREFIX = "demo:login:user:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisCacheService redisCacheService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public DemoInviteResponse createInvite(DemoInviteRequest request) {
        String identifier = request.getIdentifier().trim();
        int ttlMinutes = request.getTtlMinutes() == null ? 120 : request.getTtlMinutes();

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateDemoCitizen(user);

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = sha256(rawToken);

        Object previousTokenHash = redisCacheService.getCache(userKey(user));
        if (previousTokenHash instanceof String previousHash && !previousHash.isBlank()) {
            redisCacheService.deleteCache(REDIS_KEY_PREFIX + previousHash);
        }

        redisCacheService.setCache(
                REDIS_KEY_PREFIX + tokenHash,
                user.getUsername(),
                ttlMinutes
        );
        redisCacheService.setCache(userKey(user), tokenHash, ttlMinutes);

        return DemoInviteResponse.builder()
                .identifier(user.getUsername())
                .token(rawToken)
                .expiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES))
                .build();
    }

    @Transactional(readOnly = true)
    public String exchangeToken(String rawToken) {
        String tokenHash = sha256(rawToken.trim());
        Object cachedIdentifier = redisCacheService.getAndDelete(
                REDIS_KEY_PREFIX + tokenHash
        );

        if (!(cachedIdentifier instanceof String identifier) || identifier.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Mã QR không hợp lệ, đã hết hạn hoặc đã được sử dụng"
            );
        }

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateDemoCitizen(user);

        Object activeTokenHash = redisCacheService.getCache(userKey(user));
        if (!(activeTokenHash instanceof String activeHash) || !tokenHash.equals(activeHash)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Mã QR đã được thay thế bằng một mã mới"
            );
        }
        redisCacheService.deleteCache(userKey(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtService.generateDemoToken(userDetails);
    }

    private void validateDemoCitizen(User user) {
        boolean active = Boolean.TRUE.equals(user.getIsActive());
        boolean citizen = user.getRole() != null
                && "CITIZEN".equals(user.getRole().getRoleName());

        if (!active || !citizen) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "QR trải nghiệm chỉ được cấp cho tài khoản Citizen đang hoạt động"
            );
        }
        if (user.getDefaultBuilding() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Hãy gán ngôi nhà trình diễn cho tài khoản trước khi tạo QR"
            );
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Không thể khởi tạo SHA-256", ex);
        }
    }

    private String userKey(User user) {
        return REDIS_USER_KEY_PREFIX + user.getUserId();
    }
}
