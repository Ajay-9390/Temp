package com.accreditation.nba.evidence.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger metadata for the Evidence Management module.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI evidenceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NBA Evidence Management API")
                        .version("v1")
                        .description("""
                                Manages the complete lifecycle of NBA accreditation evidence:
                                creation, upload, validation, storage, metadata extraction, mapping,
                                review, approval/rejection, versioning, audit, search, gap detection,
                                statistics and reports.

                                Authentication is intentionally absent in this development phase; a mock
                                identity is used (send optional X-User-Id / X-User-Name headers). This module
                                references external modules (Program, Department, Academic Year, NBA Criteria)
                                by ID only.""")
                        .contact(new Contact().name("Evidence Management Module"))
                        .license(new License().name("Internal / Project")));
    }
}
