package com.example.nba.institution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update payload for an institution. */
@Schema(description = "Institution create/update request")
public record InstitutionRequest(

        @Schema(example = "ABC Institute of Technology")
        @NotBlank(message = "name is required")
        @Size(max = 200)
        String name,

        @Schema(example = "ABC-INST", description = "Unique institution code")
        @NotBlank(message = "code is required")
        @Size(max = 50)
        String code,

        @Schema(example = "AUTONOMOUS")
        @Size(max = 100)
        String type,

        @Size(max = 500) String address,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 255) String website,

        @Email(message = "contactEmail must be a valid email")
        @Size(max = 255)
        String contactEmail,

        @Size(max = 50) String contactPhone
) {
}
