package com.supplychainx.security.config;

import com.supplychainx.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/", "/health", "/api/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        
                        // MODULE APPROVISIONNEMENT
                        .requestMatchers("/api/suppliers/**")
                        .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "RESPONSABLE_ACHATS", "ADMIN")
                        
                        .requestMatchers("/api/raw-materials/**")
                        .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "SUPERVISEUR_LOGISTIQUE", "ADMIN")
                        
                        .requestMatchers("/api/supply-orders/**")
                        .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "RESPONSABLE_ACHATS", "ADMIN")
                        
                        // MODULE PRODUCTION
                        .requestMatchers("/api/products/**")
                        .hasAnyRole("CHEF_PRODUCTION", "SUPERVISEUR_PRODUCTION", "PLANIFICATEUR", "ADMIN")
                        
                        .requestMatchers("/api/bill-of-materials/**")
                        .hasAnyRole("CHEF_PRODUCTION", "PLANIFICATEUR", "ADMIN")
                        
                        .requestMatchers("/api/production-orders/**")
                        .hasAnyRole("CHEF_PRODUCTION", "SUPERVISEUR_PRODUCTION", "PLANIFICATEUR", "ADMIN")
                        
                        // MODULE LIVRAISON
                        .requestMatchers("/api/customers/**")
                        .hasAnyRole("GESTIONNAIRE_COMMERCIAL", "ADMIN")
                        
                        .requestMatchers("/api/orders/**")
                        .hasAnyRole("GESTIONNAIRE_COMMERCIAL", "RESPONSABLE_LOGISTIQUE", "ADMIN")
                        
                        .requestMatchers("/api/deliveries/**")
                        .hasAnyRole("RESPONSABLE_LOGISTIQUE", "SUPERVISEUR_LIVRAISONS", "ADMIN")
                        
                        // Admin uniquement
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            String timestamp = java.time.LocalDateTime.now().toString();
                            String json = String.format(
                                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                                timestamp, authException.getMessage(), request.getRequestURI()
                            );
                            response.getWriter().write(json);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            String timestamp = java.time.LocalDateTime.now().toString();
                            String json = String.format(
                                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\",\"path\":\"%s\"}",
                                timestamp, request.getRequestURI()
                            );
                            response.getWriter().write(json);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
