package com.supplychainx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.common.repository.UserRepository;
import com.supplychainx.security.dto.AuthResponse;
import com.supplychainx.security.dto.LoginRequest;
import com.supplychainx.security.dto.RefreshTokenRequest;
import com.supplychainx.security.entity.RefreshToken;
import com.supplychainx.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour le module de sécurité JWT
 * Tests réels avec base H2 en mémoire, sans mocks
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration - Sécurité JWT")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User adminUser;
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Créer utilisateur test standard
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@test.com");
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setRole(UserRole.GESTIONNAIRE_APPROVISIONNEMENT);
        testUser = userRepository.save(testUser);

        // Créer utilisateur admin
        adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepository.save(adminUser);
    }

    // ==================== TESTS D'AUTHENTIFICATION ====================

    @Test
    @DisplayName("Login valide → retourne Access Token + Refresh Token")
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest(testUser.getEmail(), TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.role").value(testUser.getRole().name()))
                .andReturn();

        // Vérifier que les tokens sont bien présents et non vides
        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo(testUser.getIdUser());
    }

    @Test
    @DisplayName("Login avec mauvais mot de passe → 401 Unauthorized")
    void testLoginInvalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest(testUser.getEmail(), "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login avec email inexistant → 401 Unauthorized")
    void testLoginUserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@test.com", TEST_PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login avec champs manquants → 400 Bad Request")
    void testLoginMissingFields() throws Exception {
        // Email manquant
        String invalidJson = "{\"password\":\"password123\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login avec email invalide → 400 Bad Request")
    void testLoginInvalidEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("not-an-email", TEST_PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS ACCÈS ENDPOINTS PROTÉGÉS ====================

    @Test
    @DisplayName("Accès endpoint protégé sans token → 401 Unauthorized")
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accès endpoint protégé avec Access Token valide → 200 OK")
    void testAccessProtectedEndpointWithValidToken() throws Exception {
        // 1. Login pour obtenir un token
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);

        // 2. Accéder à un endpoint protégé
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Accès endpoint avec token malformé → 401 Unauthorized")
    void testAccessWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer invalid-token-format")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accès endpoint avec token sans préfixe Bearer → 401 Unauthorized")
    void testAccessWithoutBearerPrefix() throws Exception {
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);

        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", authResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accès endpoint admin sans rôle admin → 403 Forbidden")
    void testAccessAdminEndpointWithoutAdminRole() throws Exception {
        // Login avec utilisateur non-admin
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);

        // Tentative d'accès à un endpoint admin
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Accès endpoint admin avec rôle admin → 200 OK")
    void testAccessAdminEndpointWithAdminRole() throws Exception {
        // Login avec utilisateur admin
        AuthResponse authResponse = performLogin(adminUser.getEmail(), TEST_PASSWORD);

        // Accès à un endpoint admin
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== TESTS REFRESH TOKEN ====================

    @Test
    @DisplayName("Refresh avec token valide → nouveau Access Token + nouveau Refresh Token")
    void testRefreshTokenSuccess() throws Exception {
        // 1. Login initial
        AuthResponse loginResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);
        String originalRefreshToken = loginResponse.getRefreshToken();

        // Petit délai pour garantir un timestamp différent (iat dans JWT)
        Thread.sleep(1500);

        // 2. Utiliser le refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(originalRefreshToken);

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andReturn();

        AuthResponse refreshResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        // Vérifier que les nouveaux tokens sont différents
        assertThat(refreshResponse.getAccessToken()).isNotEqualTo(loginResponse.getAccessToken());
        assertThat(refreshResponse.getRefreshToken()).isNotEqualTo(originalRefreshToken);
    }

    @Test
    @DisplayName("Refresh Token Rotation → ancien token devient invalide")
    void testRefreshTokenRotation() throws Exception {
        // 1. Login initial
        AuthResponse loginResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);
        String originalRefreshToken = loginResponse.getRefreshToken();

        // 2. Premier refresh
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(originalRefreshToken);
        
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        // 3. Tentative de réutiliser l'ancien refresh token → doit échouer
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh avec token invalide → 401 Unauthorized")
    void testRefreshWithInvalidToken() throws Exception {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh avec token révoqué → 401 Unauthorized")
    void testRefreshWithRevokedToken() throws Exception {
        // 1. Login pour obtenir un refresh token
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);

        // 2. Révoquer manuellement le token en base
        RefreshToken token = refreshTokenRepository.findByToken(authResponse.getRefreshToken())
                .orElseThrow();
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // 3. Tentative de refresh avec le token révoqué
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(authResponse.getRefreshToken());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TESTS LOGOUT ====================

    @Test
    @DisplayName("Logout invalide le refresh token")
    void testLogoutRevokesRefreshToken() throws Exception {
        // 1. Login
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);
        String refreshToken = authResponse.getRefreshToken();

        // 2. Vérifier que le token existe et n'est pas révoqué
        RefreshToken tokenBeforeLogout = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow();
        assertThat(tokenBeforeLogout.isRevoked()).isFalse();

        // 3. Logout
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // 4. Vérifier que le token est maintenant révoqué
        RefreshToken tokenAfterLogout = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow();
        assertThat(tokenAfterLogout.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Tentative de refresh après logout → 401 Unauthorized")
    void testRefreshAfterLogout() throws Exception {
        // 1. Login
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);
        String refreshToken = authResponse.getRefreshToken();

        // 2. Logout
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // 3. Tentative de refresh avec le token révoqué
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout avec token invalide → 401 Unauthorized")
    void testLogoutWithInvalidToken() throws Exception {
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken("invalid-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TESTS ISOLATION & SÉCURITÉ ====================

    @Test
    @DisplayName("Aucun endpoint /api/** accessible sans JWT valide")
    void testAllApiEndpointsRequireAuthentication() throws Exception {
        // Tester plusieurs endpoints API
        String[] protectedEndpoints = {
                "/api/suppliers",
                "/api/raw-materials",
                "/api/products",
                "/api/production-orders"
        };

        for (String endpoint : protectedEndpoints) {
            mockMvc.perform(get(endpoint)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Les endpoints publics restent accessibles sans authentification")
    void testPublicEndpointsAccessible() throws Exception {
        // Page d'accueil
        mockMvc.perform(get("/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Health check
        mockMvc.perform(get("/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Vérification des statuts HTTP pour scénarios d'erreur")
    void testHttpStatusCodes() throws Exception {
        // 400 Bad Request - données invalides
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());

        // 401 Unauthorized - credentials invalides
        LoginRequest badLogin = new LoginRequest(testUser.getEmail(), "wrongpassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());

        // 401 Unauthorized - pas de token
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isUnauthorized());

        // 403 Forbidden - rôle insuffisant
        AuthResponse authResponse = performLogin(testUser.getEmail(), TEST_PASSWORD);
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                .andExpect(status().isForbidden());

        // 200 OK - requête valide
        mockMvc.perform(get("/api/suppliers")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                .andExpect(status().isOk());
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Effectue un login et retourne la réponse d'authentification
     */
    private AuthResponse performLogin(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
    }
}
