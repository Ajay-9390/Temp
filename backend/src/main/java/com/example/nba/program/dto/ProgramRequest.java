package com.example.nba.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Create/update payload for a program. */
@Schema(description = "Program create/update request")
public record ProgramRequest(

        @Schema(description = "Owning department id (immutable after creation)")
        @NotNull(message = "departmentId is required")
        UUID departmentId,

        @Schema(example = "B.Tech Computer Science and Engineering")
        @NotBlank(message = "name is required")
        @Size(max = 200)
        String name,

        @Schema(example = "BTECH-CSE", description = "Unique within the department")
        @NotBlank(message = "code is required")
        @Size(max = 50)
        String code,

        @Schema(example = "B.Tech")
        @Size(max = 100) String degree,

        @Schema(example = "Computer Science and Engineering")
        @Size(max = 150) String branch,

        @Size(max = 2000) String description,

        @Schema(example = "4")
        @NotNull(message = "durationYears is required")
        @Positive(message = "durationYears must be greater than 0")
        Integer durationYears,

        @Schema(example = "8")
        @NotNull(message = "totalSemesters is required")
        @Positive(message = "totalSemesters must be greater than 0")
        Integer totalSemesters,

        @Schema(example = "120")
        @NotNull(message = "intake is required")
        @PositiveOrZero(message = "intake must be zero or greater")
        Integer intake,

        @Schema(example = "2010")
        @Min(value = 1800, message = "establishedYear is not valid")
        Integer establishedYear
) {
}
