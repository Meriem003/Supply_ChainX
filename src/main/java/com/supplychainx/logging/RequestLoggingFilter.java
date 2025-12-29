package com.supplychainx.logging;

import com.supplychainx.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            String endpoint = request.getMethod() + " " + request.getRequestURI();
            LoggingContext.setEndpoint(endpoint);
            LoggingContext.setLogType(LoggingContext.LogType.APPLICATION);

            extractUserInfoFromToken(request);

            log.info("Incoming request: {} from IP: {}", endpoint, request.getRemoteAddr());

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            LoggingContext.setHttpStatus(response.getStatus());

            if (response.getStatus() >= 400) {
                log.warn("Request completed with error: {} - Status: {} - Duration: {}ms",
                    endpoint, response.getStatus(), duration);
            } else {
                log.info("Request completed successfully: {} - Status: {} - Duration: {}ms",
                    endpoint, response.getStatus(), duration);
            }

        } finally {
            LoggingContext.clear();
        }
    }

    private void extractUserInfoFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isTokenValid(token)) {
                    Long userId = jwtUtil.extractUserId(token);
                    String role = jwtUtil.extractRole(token);

                    LoggingContext.setUserId(userId);
                    LoggingContext.setUserRole(role);
                }
            } catch (Exception e) {
                log.debug("Failed to extract user info from token: {}", e.getMessage());
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
               path.startsWith("/health") ||
               path.equals("/favicon.ico");
    }
}

