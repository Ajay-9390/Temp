package com.nba.attainment.config;

import com.nba.attainment.dto.response.ErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Swagger UI is available at: {@code /swagger-ui/index.html}
 * OpenAPI JSON is available at: {@code /api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort)
                                    .description("Local development server")
                ))
                .components(new Components()
                        .addSchemas("ErrorResponse", errorResponseSchema())
                        .addResponses("NotFound",        standardErrorResponse("Resource not found"))
                        .addResponses("BadRequest",      standardErrorResponse("Invalid request parameters"))
                        .addResponses("UnprocessableEntity", standardErrorResponse("Business validation failed"))
                        .addResponses("InternalError",   standardErrorResponse("Unexpected server error"))
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("PO/PSO Attainment API")
                .description("""
                        NBA Module 09 — Program Outcome and Program Specific Outcome Attainment.
                        
                        This API provides:
                        - PO attainment calculation using configurable strategies (default: weighted average)
                        - PSO attainment calculation
                        - Combined PO+PSO calculation
                        - Full calculation trace drill-down (course → CO → mapping → formula)
                        - Dashboard summary with averages, highest/lowest values
                        
                        **Calculation Formula (Weighted Average):**
                        ```
                        Attainment(PO) = Σ(CO_attainment × mapping_level) / Σ(mapping_level)
                        ```
                        Mapping levels: 0=None, 1=Low, 2=Medium, 3=High
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("NBA Attainment Module Team")
                        .email("attainment@nba.example.com"))
                .license(new License().name("Internal Use Only"));
    }

    @SuppressWarnings("rawtypes")
    private Schema errorResponseSchema() {
        return new Schema<ErrorResponse>()
                .type("object")
                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperty("status",    new Schema<>().type("integer"))
                .addProperty("error",     new Schema<>().type("string")
                        .example("VALIDATION_ERROR"))
                .addProperty("message",   new Schema<>().type("string")
                        .example("academicYear is required"))
                .addProperty("path",      new Schema<>().type("string")
                        .example("/api/v1/attainment/po/calculate"));
    }

    private ApiResponse standardErrorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                ));
    }
}
