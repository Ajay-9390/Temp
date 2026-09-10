package com.nba.attainment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request body for triggering a PSO attainment calculation.
 */
public record PSOAttainmentCalculationRequest(

        @NotNull(message = "programId is required")
        UUID programId,

        @NotBlank(message = "academicYear is required")
        @Pattern(regexp = "^\\d{4}-\\d{2,4}$", message = "academicYear must match format YYYY-YY or YYYY-YYYY (e.g. 2023-24)")
        String academicYear,

        String calculationMethod,

        String calculationVersion
) {
    public String resolvedMethod()  { return calculationMethod  != null ? calculationMethod  : "WEIGHTED"; }
    public String resolvedVersion() { return calculationVersion != null ? calculationVersion : "v1"; }
}
