package com.example.nba.academic.semester.dto;

import com.example.nba.academic.AcademicLifecycleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SemesterResponse(
        UUID id,
        UUID academicYearId,
        Integer semesterNumber,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        AcademicLifecycleStatus status,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
