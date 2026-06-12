package com.example.GuardBatXat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    // Giới hạn 60 request / 1 phút / 1 IP
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Bỏ qua các request tĩnh hoặc cấu hình chung (nếu cần)
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = "rate_limit:" + clientIp;

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null && currentRequests == 1) {
            // Lần đầu tiên gọi, set TTL là 1 phút
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        if (currentRequests != null && currentRequests > MAX_REQUESTS_PER_MINUTE) {
            // Trả về HTTP 429 Too Many Requests
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 429, \"message\": \"Quá nhiều yêu cầu. Hệ thống chống Spam đang kích hoạt. Vui lòng đợi 1 phút.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
