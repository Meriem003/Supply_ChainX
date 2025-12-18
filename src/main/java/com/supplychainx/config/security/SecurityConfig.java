package com.supplychainx.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/health",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                .requestMatchers("/api/raw-materials/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_APPROVISIONNEMENT",
                    "RESPONSABLE_ACHATS"
                )
                
                .requestMatchers("/api/suppliers/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_APPROVISIONNEMENT",
                    "RESPONSABLE_ACHATS"
                )
                
                .requestMatchers(HttpMethod.GET, "/api/supply-orders/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_APPROVISIONNEMENT",
                    "RESPONSABLE_ACHATS",
                    "CHEF_PRODUCTION"
                )
                .requestMatchers(HttpMethod.POST, "/api/supply-orders/**").hasAnyRole(
                    "ADMIN",
                    "RESPONSABLE_ACHATS"
                )
                .requestMatchers(HttpMethod.PUT, "/api/supply-orders/**").hasAnyRole(
                    "ADMIN",
                    "RESPONSABLE_ACHATS",
                    "GESTIONNAIRE_APPROVISIONNEMENT"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/supply-orders/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole(
                    "ADMIN",
                    "CHEF_PRODUCTION",
                    "PLANIFICATEUR",
                    "SUPERVISEUR_PRODUCTION",
                    "GESTIONNAIRE_COMMERCIAL"
                )
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole(
                    "ADMIN",
                    "CHEF_PRODUCTION"
                )
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole(
                    "ADMIN",
                    "CHEF_PRODUCTION"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.GET, "/api/production-orders/**").hasAnyRole(
                    "ADMIN",
                    "CHEF_PRODUCTION",
                    "PLANIFICATEUR",
                    "SUPERVISEUR_PRODUCTION"
                )
                .requestMatchers(HttpMethod.POST, "/api/production-orders/**").hasAnyRole(
                    "ADMIN",
                    "PLANIFICATEUR",
                    "CHEF_PRODUCTION"
                )
                .requestMatchers(HttpMethod.PUT, "/api/production-orders/**").hasAnyRole(
                    "ADMIN",
                    "PLANIFICATEUR",
                    "SUPERVISEUR_PRODUCTION"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/production-orders/**").hasRole("ADMIN")
                
                .requestMatchers("/api/bom/**").hasAnyRole(
                    "ADMIN",
                    "CHEF_PRODUCTION",
                    "PLANIFICATEUR"
                )
                
                .requestMatchers("/api/planning/**").hasAnyRole(
                    "ADMIN",
                    "PLANIFICATEUR",
                    "CHEF_PRODUCTION"
                )
                
                .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL",
                    "RESPONSABLE_LOGISTIQUE",
                    "SUPERVISEUR_LIVRAISONS"
                )
                .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL"
                )
                .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL",
                    "RESPONSABLE_LOGISTIQUE",
                    "SUPERVISEUR_LIVRAISONS",
                    "PLANIFICATEUR"
                )
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL"
                )
                .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole(
                    "ADMIN",
                    "GESTIONNAIRE_COMMERCIAL",
                    "RESPONSABLE_LOGISTIQUE"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.GET, "/api/deliveries/**").hasAnyRole(
                    "ADMIN",
                    "RESPONSABLE_LOGISTIQUE",
                    "SUPERVISEUR_LIVRAISONS",
                    "SUPERVISEUR_LOGISTIQUE",
                    "GESTIONNAIRE_COMMERCIAL"
                )
                .requestMatchers(HttpMethod.POST, "/api/deliveries/**").hasAnyRole(
                    "ADMIN",
                    "RESPONSABLE_LOGISTIQUE",
                    "SUPERVISEUR_LOGISTIQUE"
                )
                .requestMatchers(HttpMethod.PUT, "/api/deliveries/**").hasAnyRole(
                    "ADMIN",
                    "RESPONSABLE_LOGISTIQUE",
                    "SUPERVISEUR_LIVRAISONS",
                    "SUPERVISEUR_LOGISTIQUE"
                )
                .requestMatchers(HttpMethod.DELETE, "/api/deliveries/**").hasRole("ADMIN")

                .requestMatchers("/api/users/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            
            .httpBasic(basic -> {})
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
