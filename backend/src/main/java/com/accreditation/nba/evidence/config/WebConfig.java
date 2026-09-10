package com.accreditation.nba.evidence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration. CORS is opened for the local frontend during development.
 * Tighten {@code evidence.cors.allowed-origins} for shared/staging environments.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${evidence.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
