package com.supplychainx.security.service;

import com.supplychainx.common.entity.User;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.security.dto.AuthResponse;
import com.supplychainx.security.dto.LoginRequest;
import com.supplychainx.security.entity.RefreshToken;
import com.supplychainx.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getIdUser(),
                user.getEmail(),
                user.getRole().name()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getIdUser())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenString);
        User user = refreshToken.getUser();

        refreshTokenService.revokeRefreshToken(refreshTokenString);

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getIdUser(),
                user.getEmail(),
                user.getRole().name()
        );

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getIdUser())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
