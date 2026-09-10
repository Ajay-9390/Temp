package com.example.nba.academic.year.dto;

import com.example.nba.academic.AcademicLifecycleStatus;
import jakarta.validation.constraints.NotNull;

public record AcademicYearStatusRequest(
        @NotNull(message = "status is required") AcademicLifecycleStatus status
) {
}
