package com.supplychainx.security.service;

import com.supplychainx.common.entity.User;
import com.supplychainx.security.entity.RefreshToken;
import com.supplychainx.security.jwt.JwtUtil;
import com.supplychainx.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String tokenString = jwtUtil.generateRefreshToken(user.getIdUser());
        
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid or revoked refresh token"));

        if (refreshToken.isExpired()) {
            throw new BadCredentialsException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));
        
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUser_IdUser(userId);
    }

    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
