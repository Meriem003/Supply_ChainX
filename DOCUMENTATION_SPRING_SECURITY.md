# 📚 Documentation Spring Security - SupplyChainX

## Table des matières
1. [Concepts fondamentaux](#concepts-fondamentaux)
2. [Architecture interne](#architecture-interne)
3. [Composants clés](#composants-clés)
4. [Bonnes pratiques](#bonnes-pratiques)
5. [Intégration ELK](#intégration-elk)

---

## Concepts fondamentaux

### Qu'est-ce que Spring Security ?

**Spring Security** est un framework de sécurité puissant et hautement personnalisable pour les applications Java. Il fournit :

- **Authentification** : Vérifier l'identité de l'utilisateur
- **Autorisation** : Contrôler l'accès aux ressources
- **Protection contre les attaques** : CSRF, XSS, Session Fixation, etc.
- **Intégration facile** : Avec Spring Boot, JWT, OAuth2, LDAP, etc.

### Principes de base

#### 1. Authentication (Authentification)

L'authentification répond à la question : **"Qui es-tu ?"**

```java
public interface Authentication extends Principal, Serializable {
    Collection<? extends GrantedAuthority> getAuthorities(); // Rôles/permissions
    Object getCredentials();                                 // Mot de passe
    Object getPrincipal();                                   // Identifiant (email, username)
    boolean isAuthenticated();                               // Est authentifié ?
}
```

**Dans SupplyChainX** :
```java
UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
    email,          // Principal
    null,           // Credentials (null car JWT, pas de password)
    authorities     // [ROLE_ADMIN]
);
```

#### 2. Authorization (Autorisation)

L'autorisation répond à la question : **"Que peux-tu faire ?"**

**Types d'autorisation** :
- **URL-based** : `/api/admin/**` requiert `ROLE_ADMIN`
- **Method-based** : `@PreAuthorize("hasRole('ADMIN')")`
- **Domain-based** : Accès selon le propriétaire de la ressource

**Dans SupplyChainX** :
```java
.requestMatchers("/api/suppliers/**")
    .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "RESPONSABLE_ACHATS", "ADMIN")
```

#### 3. SecurityContext

Le **SecurityContext** contient les informations de sécurité de l'utilisateur courant.

```java
// Stocker l'authentification
SecurityContextHolder.getContext().setAuthentication(authentication);

// Récupérer l'utilisateur courant
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String email = auth.getPrincipal().toString();
```

**⚠️ Thread-local** : Le SecurityContext est lié au thread courant. Dans une API stateless, il est recréé à chaque requête.

#### 4. GrantedAuthority (Rôles)

Une **GrantedAuthority** représente un rôle ou une permission.

```java
SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
```

**Convention Spring Security** : Les rôles doivent être préfixés par `ROLE_`.

**Dans SupplyChainX** :
```java
// Stocké en base : "ADMIN"
// Spring Security : "ROLE_ADMIN"
SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
```

---

## Architecture interne

### Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY ARCHITECTURE                      │
└─────────────────────────────────────────────────────────────────────┘

                             HTTP Request
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  DelegatingFilterProxy   │  Pont entre Servlet et Spring
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  FilterChainProxy        │  Gère toutes les SecurityFilterChain
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  SecurityFilterChain     │  Liste de filtres de sécurité
                    └─────────────┬────────────┘
                                  │
                 ┌────────────────┼────────────────┐
                 │                │                │
                 ▼                ▼                ▼
       ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
       │   Filter 1  │  │   Filter 2  │  │   Filter N  │
       │ (CSRF)      │  │ (JWT Auth)  │  │ (Exception) │
       └─────────────┘  └─────────────┘  └─────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  SecurityContext         │  Contient Authentication
                    └─────────────┬────────────┘
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  Controller              │  Logique métier
                    └──────────────────────────┘
```

### Cycle de vie d'une requête

```
1. HTTP Request arrive
   │
2. DelegatingFilterProxy intercepte
   │
3. FilterChainProxy sélectionne la SecurityFilterChain appropriée
   │
4. Exécution séquentielle des filtres :
   │
   ├─ SecurityContextPersistenceFilter : Charge le SecurityContext (vide en stateless)
   │
   ├─ HeaderWriterFilter : Ajoute des headers de sécurité
   │
   ├─ CsrfFilter : Vérifie le token CSRF (désactivé pour API REST)
   │
   ├─ JwtAuthenticationFilter (CUSTOM) : 🔑 NOTRE FILTRE
   │   ├─ Extrait JWT du header Authorization
   │   ├─ Valide signature et expiration
   │   ├─ Extrait userId, email, role
   │   ├─ Crée UsernamePasswordAuthenticationToken
   │   └─ Configure SecurityContextHolder
   │
   ├─ ExceptionTranslationFilter : Gère les exceptions de sécurité
   │   └─ AuthenticationException → 401
   │   └─ AccessDeniedException → 403
   │
   └─ FilterSecurityInterceptor : 🔒 AUTORISATION
       ├─ Vérifie les règles de sécurité (.hasRole(), .authenticated())
       ├─ Compare le rôle utilisateur avec les rôles requis
       └─ Autorise ou refuse l'accès
   │
5. Si OK → Controller appelé
   │
6. Response retournée
```

---

## Composants clés

### 1. SecurityConfig

**Rôle** : Configuration centrale de la sécurité.

**Annotations** :
- `@Configuration` : Classe de configuration Spring
- `@EnableWebSecurity` : Active Spring Security
- `@EnableMethodSecurity` : Active les annotations de sécurité sur les méthodes

**Dans SupplyChainX** :
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (API REST stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurer les autorisations
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/health").permitAll()
                .requestMatchers("/api/suppliers/**")
                    .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "ADMIN")
                .anyRequest().authenticated()
            )
            
            // Session stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Gestion des erreurs
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            
            // Ajouter notre filtre JWT
            .addFilterBefore(jwtAuthenticationFilter, 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Concepts clés** :
- **permitAll()** : Accessible sans authentification
- **authenticated()** : Nécessite d'être authentifié
- **hasRole()** : Nécessite un rôle spécifique
- **hasAnyRole()** : Nécessite au moins un des rôles

### 2. JwtAuthenticationFilter

**Rôle** : Intercepter chaque requête pour valider le JWT.

**Héritage** : `OncePerRequestFilter` (exécuté une seule fois par requête)

**Dans SupplyChainX** :
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraire le token du header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 2. Valider le token
            if (jwtUtil.isTokenValid(token)) {
                
                // 3. Extraire les informations utilisateur
                String email = jwtUtil.extractEmail(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // 4. Créer une Authentication
                SimpleGrantedAuthority authority = 
                    new SimpleGrantedAuthority("ROLE_" + role);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(authority)
                    );

                // 5. Configurer le SecurityContext
                SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

                // 6. Enrichir les logs
                LoggingContext.setUserId(userId);
                LoggingContext.setUserRole(role);
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
        }

        // 7. Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
```

**Points importants** :
- ✅ Exécuté **avant** les autres filtres de sécurité
- ✅ N'intercepte pas les endpoints publics (vérification faite par FilterSecurityInterceptor)
- ✅ En cas d'erreur, la requête continue (gérée par ExceptionTranslationFilter)

### 3. AuthenticationManager

**Rôle** : Coordonner l'authentification avec différents providers.

**Dans une application classique** :
```java
@Bean
public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
) throws Exception {
    return config.getAuthenticationManager();
}
```

**Dans SupplyChainX** : ❌ **Non utilisé** car nous gérons l'authentification manuellement via JWT.

**Pourquoi ?**
- L'AuthenticationManager est conçu pour des authentifications avec credentials (username/password)
- Avec JWT, on ne vérifie que la validité du token (signature, expiration)
- Pas besoin de requêter la base de données à chaque requête (stateless)

### 4. UserDetailsService

**Rôle** : Charger les informations utilisateur depuis la base de données.

**Interface** :
```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

**Dans SupplyChainX** : ❌ **Non implémenté** car inutile avec JWT stateless.

**Si implémenté** (optionnel) :
```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) 
        throws UsernameNotFoundException {
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .roles(user.getRole().name())
            .build();
    }
}
```

**Quand l'utiliser ?**
- Authentification basée sur formulaire (session)
- OAuth2 / LDAP
- Remember-me functionality

### 5. PasswordEncoder

**Rôle** : Hasher et vérifier les mots de passe de manière sécurisée.

**Dans SupplyChainX** :
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Utilisation** :
```java
// Lors de la création d'un utilisateur
String hashedPassword = passwordEncoder.encode(rawPassword);
user.setPassword(hashedPassword);

// Lors du login
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
```

**Pourquoi BCrypt ?**
- ✅ Adaptatif : on peut augmenter la complexité avec le temps
- ✅ Salted : chaque hash est unique même pour le même password
- ✅ Lent : rend le brute-force difficile
- ✅ Standard de l'industrie

**Force de hachage** :
```java
// Par défaut : 10 rounds
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// Personnalisé : 12 rounds (plus sécurisé mais plus lent)
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
```

### 6. ExceptionHandling

**Rôle** : Gérer les erreurs d'authentification et d'autorisation.

**Dans SupplyChainX** :
```java
.exceptionHandling(ex -> ex
    // 401 Unauthorized : Token invalide/manquant
    .authenticationEntryPoint((request, response, authException) -> {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        String json = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\"," +
            "\"message\":\"%s\",\"path\":\"%s\"}",
            LocalDateTime.now(),
            authException.getMessage(),
            request.getRequestURI()
        );
        
        response.getWriter().write(json);
    })
    
    // 403 Forbidden : Rôle insuffisant
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        
        String json = String.format(
            "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\"," +
            "\"message\":\"Access denied\",\"path\":\"%s\"}",
            LocalDateTime.now(),
            request.getRequestURI()
        );
        
        response.getWriter().write(json);
    })
)
```

**Différence 401 vs 403** :
- **401 Unauthorized** : "Je ne sais pas qui tu es" (pas authentifié)
- **403 Forbidden** : "Je sais qui tu es, mais tu n'as pas le droit" (authentifié mais rôle insuffisant)

---

## Bonnes pratiques

### 1. Sécurisation des API REST

#### ✅ À FAIRE

**Utiliser HTTPS en production**
```properties
# application.properties (production)
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
```

**Désactiver CSRF pour les API REST**
```java
http.csrf(AbstractHttpConfigurer::disable)
```
**Pourquoi ?** Les API REST sont stateless et utilisent des tokens, pas de cookies.

**Configurer CORS correctement**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://app.example.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Implémenter un Rate Limiting**
```java
// Avec Bucket4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(...) {
        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
        }
    }
}
```

**Valider toutes les entrées**
```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
) {
    // @Valid déclenche la validation
}

public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
```

#### ❌ À ÉVITER

**Exposer des informations sensibles**
```java
// ❌ BAD
throw new BadCredentialsException("User with email " + email + " not found");

// ✅ GOOD
throw new BadCredentialsException("Invalid credentials");
```

**Logger des données sensibles**
```java
// ❌ BAD
log.info("User {} logged in with password {}", email, password);

// ✅ GOOD
log.info("User {} logged in successfully", email);
```

**Utiliser des secrets en dur**
```java
// ❌ BAD
jwt.secret=MySecretKey123

// ✅ GOOD
jwt.secret=${JWT_SECRET}
```

**Accepter tous les CORS**
```java
// ❌ BAD
configuration.setAllowedOrigins(Arrays.asList("*"));

// ✅ GOOD
configuration.setAllowedOrigins(Arrays.asList("https://app.example.com"));
```

### 2. Gestion des tokens JWT

#### Durée de vie optimale

```properties
# Access Token : court (15 minutes)
jwt.access-token-expiration=900000

# Refresh Token : long (7 jours)
jwt.refresh-token-expiration=604800000
```

**Pourquoi ces valeurs ?**
- **15 min** : Limite la fenêtre d'exploitation en cas de vol du token
- **7 jours** : Évite de redemander le mot de passe trop souvent

#### Rotation des Refresh Tokens

```java
// ✅ OBLIGATOIRE : Révoquer l'ancien token lors du refresh
@Transactional
public AuthResponse refreshAccessToken(String refreshTokenString) {
    RefreshToken refreshToken = verifyRefreshToken(refreshTokenString);
    
    // 1. Révoquer l'ancien token
    revokeRefreshToken(refreshTokenString);
    
    // 2. Générer un nouveau Access Token
    String newAccessToken = jwtUtil.generateAccessToken(...);
    
    // 3. Générer un nouveau Refresh Token
    RefreshToken newRefreshToken = createRefreshToken(user);
    
    return new AuthResponse(newAccessToken, newRefreshToken.getToken(), ...);
}
```

**Avantages** :
- ✅ Détecte les tentatives de réutilisation (token déjà révoqué)
- ✅ Limite la durée de vie effective d'un Refresh Token
- ✅ Meilleure traçabilité en base de données

#### Blacklist de tokens (optionnel)

Pour révoquer immédiatement un Access Token compromis :

```java
@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token, long expirationTime) {
        // Stocker dans Redis avec TTL = durée restante du token
        redisTemplate.opsForValue().set(
            "blacklist:" + token,
            "revoked",
            expirationTime,
            TimeUnit.SECONDS
        );
    }
    
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
```

### 3. Logs de sécurité

#### Événements à logger

✅ **Toujours logger** :
- Tentatives de login (succès/échec)
- Accès refusés (401, 403)
- Tokens expirés ou invalides
- Refresh token utilisé
- Logout
- Modifications de rôles/permissions

❌ **Ne JAMAIS logger** :
- Mots de passe (même hashés)
- Tokens JWT complets
- Secrets API
- Clés de chiffrement

#### Exemple d'aspect de logging

**Dans SupplyChainX** :
```java
@Aspect
@Component
@Slf4j
public class SecurityLoggingAspect {

    @AfterReturning("execution(* com.supplychainx.security.service.AuthService.login(..))")
    public void logLoginSuccess(JoinPoint joinPoint) {
        LoggingContext.setLogType(LogType.SECURITY);
        log.info("SECURITY_EVENT: Login successful");
    }

    @AfterThrowing(
        pointcut = "execution(* com.supplychainx.security.service.AuthService.login(..))",
        throwing = "exception"
    )
    public void logLoginFailure(JoinPoint joinPoint, Exception exception) {
        LoggingContext.setLogType(LogType.SECURITY);
        log.warn("SECURITY_EVENT: Login failed - Reason: {}", exception.getMessage());
    }
}
```

### 4. Tests de sécurité

#### Tests d'intégration essentiels

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Test
    void testAccessProtectedEndpointWithoutToken() {
        mockMvc.perform(get("/api/suppliers"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessWithValidToken() {
        String token = performLogin();
        
        mockMvc.perform(get("/api/suppliers")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void testAccessWithInsufficientRole() {
        String token = performLoginAsNonAdmin();
        
        mockMvc.perform(get("/api/admin/stats")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void testRefreshTokenRotation() {
        String originalRefreshToken = performLogin().getRefreshToken();
        
        // Premier refresh
        AuthResponse response1 = performRefresh(originalRefreshToken);
        
        // Tentative de réutiliser l'ancien token
        mockMvc.perform(post("/auth/refresh")
                .content(originalRefreshToken))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## Intégration ELK

### Pourquoi ELK pour la sécurité ?

**ELK (Elasticsearch, Logstash, Kibana)** permet de :
- ✅ **Centraliser** tous les logs de sécurité
- ✅ **Analyser** les patterns d'attaque
- ✅ **Alerter** sur des comportements suspects
- ✅ **Auditer** les accès et actions utilisateurs
- ✅ **Tracer** le cycle de vie des tokens

### Architecture d'intégration

```
Spring Boot Application
        │
        ├─ SecurityLoggingAspect
        │   └─ Log events: login, logout, 401, 403
        │
        ├─ JwtAuthenticationFilter
        │   └─ Enrichit MDC: userId, role, endpoint
        │
        ├─ Logback-spring.xml
        │   └─ Appender Logstash (TCP:5000)
        │
        ▼
    Logstash
        │
        ├─ Filter logs by type (SECURITY, BUSINESS, APPLICATION)
        ├─ Detect sensitive data (password, token)
        ├─ Convert numeric fields (user_id, http_status)
        │
        ▼
    Elasticsearch
        │
        └─ Index: supplychainx-logs-{date}
        │
        ▼
    Kibana
        │
        └─ Dashboards, Searches, Alerts
```

### Configuration Logback

```xml
<configuration>
    <!-- Appender Logstash TCP -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- Champs MDC inclus -->
            <includeMdcKeyName>user_id</includeMdcKeyName>
            <includeMdcKeyName>user_role</includeMdcKeyName>
            <includeMdcKeyName>endpoint</includeMdcKeyName>
            <includeMdcKeyName>http_status</includeMdcKeyName>
            <includeMdcKeyName>log_type</includeMdcKeyName>
            <customFields>{"application":"supplychain-management"}</customFields>
        </encoder>
    </appender>
</configuration>
```

### Enrichissement MDC dans JwtAuthenticationFilter

```java
if (jwtUtil.isTokenValid(token)) {
    Long userId = jwtUtil.extractUserId(token);
    String role = jwtUtil.extractRole(token);
    
    // Enrichir le contexte de logs
    LoggingContext.setUserId(userId);
    LoggingContext.setUserRole(role);
    LoggingContext.setEndpoint(request.getRequestURI());
    LoggingContext.setLogType(LogType.SECURITY);
    
    // ...
}
```

### Recherches Kibana utiles

**Tentatives de login échouées (dernier 24h)** :
```
log_type: "SECURITY" AND message: "Login failed" AND @timestamp: [now-24h TO now]
```

**Erreurs 401 par utilisateur** :
```
http_status: 401 AND user_id: * | stats count by user_id
```

**Tokens expirés** :
```
message: "Token expiré" OR message: "Token expired"
```

**Activité suspecte (nombreux 401 depuis même IP)** :
```
http_status: 401 | stats count by source_ip | where count > 50
```

### Alertes automatiques

**Exemple : Trop de tentatives de login échouées** :

1. Créer une alerte dans Kibana
2. Condition : `count > 10` sur 5 minutes
3. Filtre : `log_type: SECURITY AND message: "Login failed"`
4. Action : Email, Slack, webhook

---

## Résumé des concepts clés

| Concept | Rôle | Implémenté dans SupplyChainX |
|---------|------|------------------------------|
| **SecurityConfig** | Configuration centrale | ✅ Oui |
| **SecurityFilterChain** | Chaîne de filtres | ✅ Oui |
| **JwtAuthenticationFilter** | Validation JWT | ✅ Oui (custom) |
| **SecurityContext** | Stockage auth | ✅ Oui (thread-local) |
| **Authentication** | Objet d'auth | ✅ UsernamePasswordAuthenticationToken |
| **GrantedAuthority** | Rôles | ✅ SimpleGrantedAuthority |
| **AuthenticationManager** | Coordination auth | ❌ Non (inutile avec JWT) |
| **UserDetailsService** | Chargement user | ❌ Non (stateless) |
| **PasswordEncoder** | Hachage password | ✅ BCryptPasswordEncoder |
| **ExceptionHandling** | Gestion erreurs | ✅ 401/403 custom |

---

## Ressources supplémentaires

### Documentation officielle
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Boot Security Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)
- [JWT RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519)

### Bonnes pratiques
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP REST Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

### Outils de test
- [Postman](https://www.postman.com/) - Tests API
- [OWASP ZAP](https://www.zaproxy.org/) - Scanner de vulnérabilités
- [JMeter](https://jmeter.apache.org/) - Tests de charge

---

**Document créé le** : 06/01/2026  
**Auteur** : Documentation Spring Security SupplyChainX
