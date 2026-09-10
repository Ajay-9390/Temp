package com.example.nba.academic.semester.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for a semester. */
@Schema(description = "Semester create/update request")
public record SemesterRequest(

        @Schema(example = "1")
        @NotNull(message = "semesterNumber is required")
        @Positive(message = "semesterNumber must be greater than 0")
        Integer semesterNumber,

        @Schema(example = "Semester 1")
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,

        LocalDate startDate,
        LocalDate endDate
) {
}
