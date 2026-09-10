package com.example.nba.academic.year.dto;

import com.example.nba.academic.AcademicLifecycleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AcademicYearResponse(
        UUID id,
        UUID programId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        AcademicLifecycleStatus status,
        long semesterCount,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
