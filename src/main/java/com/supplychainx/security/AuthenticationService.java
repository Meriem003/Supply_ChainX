package com.supplychainx.security;

import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new UnauthorizedException("Email est requis dans le header");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new UnauthorizedException("Mot de passe est requis dans le header");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        return user;
    }

    public void checkRole(User user, UserRole[] requiredRoles) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        for (UserRole role : requiredRoles) {
            if (user.getRole() == role) {
                return;
            }
        }

        throw new UnauthorizedException("Vous n'avez pas la permission d'accéder à cette ressource");
    }
}
