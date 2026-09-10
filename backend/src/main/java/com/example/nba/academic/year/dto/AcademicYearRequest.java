package com.example.nba.academic.year.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for an academic year. */
@Schema(description = "Academic year create/update request")
public record AcademicYearRequest(

        @Schema(example = "2026-27")
        @NotBlank(message = "name is required")
        @Size(max = 50)
        String name,

        @NotNull(message = "startDate is required")
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        LocalDate endDate
) {
}
