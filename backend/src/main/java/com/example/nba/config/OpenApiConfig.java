package com.example.nba.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger metadata. Swagger UI is available at {@code /swagger-ui.html}.
 * Declares a bearer-JWT security scheme (Keycloak) so "Authorize" works in the UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nbaOpenAPI() {
        final String scheme = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("NBA Program Management API")
                        .version("v1")
                        .description("""
                                Manages Institution → Department → Program → AccreditationCycle
                                and AcademicYear → Semester for the NBA accreditation platform.
                                Authorization is permission-based via Keycloak JWT authorities.""")
                        .contact(new Contact().name("NBA Platform Team"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak-issued access token")));
    }
}
