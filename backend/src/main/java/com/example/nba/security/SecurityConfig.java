package com.example.nba.security;

import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.response.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Resource-server security.
 *
 * <p>When {@code app.security.enabled=true} the module validates Keycloak JWTs and enforces
 * {@code @PreAuthorize} permission checks. When false (local standalone dev) all requests
 * are permitted so the module runs without a Keycloak instance. Authorization annotations
 * remain on controllers either way, so enabling security requires no controller changes.</p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/actuator/health/**", "/actuator/info", "/actuator/prometheus"
    };

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(PUBLIC_PATHS).permitAll()
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((req, res, e) -> writeError(res, HttpStatus.UNAUTHORIZED,
                                    ErrorCode.UNAUTHORIZED, "Authentication required"))
                            .accessDeniedHandler((req, res, e) -> writeError(res, HttpStatus.FORBIDDEN,
                                    ErrorCode.FORBIDDEN, "You do not have permission to perform this action")));
        } else {
            // Standalone dev: permit everything. Method-security annotations are effectively
            // bypassed because the mock authentication (see DevSecurityFilter) grants all authorities.
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            // Must run AFTER SecurityContextHolderFilter, otherwise that filter overwrites the
            // context we set with the (empty) repository context and @PreAuthorize denies access.
            http.addFilterAfter(new DevAuthenticationFilter(),
                    org.springframework.security.web.context.SecurityContextHolderFilter.class);
        }
        return http.build();
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse res, HttpStatus status,
                            ErrorCode code, String message) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");
        objectMapper.writeValue(res.getWriter(), ApiError.of(code.name(), message));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Correlation-Id"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
