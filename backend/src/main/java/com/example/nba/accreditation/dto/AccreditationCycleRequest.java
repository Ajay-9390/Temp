package com.example.nba.accreditation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for an accreditation cycle. */
@Schema(description = "Accreditation cycle create/update request")
public record AccreditationCycleRequest(

        @Schema(example = "NBA 2026")
        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @Schema(example = "TIER_I", description = "Configurable tier code (e.g. TIER_I, TIER_II)")
        @NotBlank(message = "tier is required")
        @Size(max = 50)
        String tier,

        @Schema(example = "GAPC v4.0")
        @NotBlank(message = "frameworkVersion is required")
        @Size(max = 100)
        String frameworkVersion,

        @Schema(example = "2026")
        @NotNull(message = "applicationYear is required")
        @Min(value = 1900, message = "applicationYear is not valid")
        Integer applicationYear,

        LocalDate startDate,
        LocalDate endDate,

        @Size(max = 2000) String remarks
) {
}
