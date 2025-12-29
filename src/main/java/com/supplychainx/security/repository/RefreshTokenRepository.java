package com.supplychainx.security.repository;

import com.supplychainx.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser_IdUser(Long userId);

    void deleteByExpiryDateBefore(LocalDateTime date);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}
