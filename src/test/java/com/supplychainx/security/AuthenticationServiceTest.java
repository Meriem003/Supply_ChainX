package com.supplychainx.security;

import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder; 

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setFirstName("Jean");
        testUser.setLastName("Dupont");
        testUser.setEmail("jean.dupont@supplychainx.com");
        testUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        testUser.setRole(UserRole.CHEF_PRODUCTION);
    }

    @Test
    void testAuthenticate_WithValidCredentials_ShouldReturnUser() {
        
        when(userRepository.findByEmail("jean.dupont@supplychainx.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword()))
                .thenReturn(true);

        User result = authenticationService.authenticate("jean.dupont@supplychainx.com", "password123");

        assertNotNull(result);
        assertEquals("Jean", result.getFirstName());
        assertEquals(UserRole.CHEF_PRODUCTION, result.getRole());
        verify(userRepository, times(1)).findByEmail("jean.dupont@supplychainx.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
    }

    @Test
    void testAuthenticate_WithInvalidEmail_ShouldThrowException() {
        when(userRepository.findByEmail("wrong@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> {
            authenticationService.authenticate("wrong@email.com", "password123");
        });
    }

    @Test
    void testAuthenticate_WithInvalidPassword_ShouldThrowException() {
        when(userRepository.findByEmail("jean.dupont@supplychainx.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword()))
                .thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> {
            authenticationService.authenticate("jean.dupont@supplychainx.com", "wrongpassword");
        });
        verify(passwordEncoder, times(1)).matches("wrongpassword", testUser.getPassword());
    }

    @Test
    void testAuthenticate_WithNullEmail_ShouldThrowException() {
        assertThrows(UnauthorizedException.class, () -> {
            authenticationService.authenticate(null, "password123");
        });
    }

    @Test
    void testAuthenticate_WithEmptyPassword_ShouldThrowException() {
        assertThrows(UnauthorizedException.class, () -> {
            authenticationService.authenticate("jean.dupont@supplychainx.com", "");
        });
    }

    @Test
    void testCheckRole_WithValidRole_ShouldNotThrowException() {
        UserRole[] requiredRoles = {UserRole.CHEF_PRODUCTION, UserRole.ADMIN};

        assertDoesNotThrow(() -> {
            authenticationService.checkRole(testUser, requiredRoles);
        });
    }

    @Test
    void testCheckRole_WithAdminUser_ShouldAlwaysPass() {
        testUser.setRole(UserRole.ADMIN);
        UserRole[] requiredRoles = {UserRole.CHEF_PRODUCTION}; 
        assertDoesNotThrow(() -> {
            authenticationService.checkRole(testUser, requiredRoles);
        });
    }

    @Test
    void testCheckRole_WithInvalidRole_ShouldThrowException() {
        UserRole[] requiredRoles = {UserRole.GESTIONNAIRE_COMMERCIAL};

        assertThrows(UnauthorizedException.class, () -> {
            authenticationService.checkRole(testUser, requiredRoles);
        });
    }
}
