package com.example.nba.academic.semester.dto;

import com.example.nba.academic.AcademicLifecycleStatus;
import jakarta.validation.constraints.NotNull;

public record SemesterStatusRequest(
        @NotNull(message = "status is required") AcademicLifecycleStatus status
) {
}
