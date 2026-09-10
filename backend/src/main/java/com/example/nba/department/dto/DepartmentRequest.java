package com.example.nba.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Create/update payload for a department. */
@Schema(description = "Department create/update request")
public record DepartmentRequest(

        @Schema(description = "Owning institution id (immutable after creation)")
        @NotNull(message = "institutionId is required")
        UUID institutionId,

        @Schema(example = "Computer Science & Engineering")
        @NotBlank(message = "name is required")
        @Size(max = 200)
        String name,

        @Schema(example = "CSE", description = "Unique within the institution")
        @NotBlank(message = "code is required")
        @Size(max = 50)
        String code,

        @Size(max = 1000) String description,

        @Schema(description = "External Keycloak/RBAC user id of the HOD")
        @Size(max = 128)
        String hodUserId
) {
}
