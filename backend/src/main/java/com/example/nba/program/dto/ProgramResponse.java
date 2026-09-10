package com.example.nba.program.dto;

import com.example.nba.program.entity.ProgramStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Full program representation. The {@code departmentName}, {@code currentAccreditation} and
 * {@code currentAcademicYear} fields are enrichment for list/detail views and may be null.
 */
public record ProgramResponse(
        UUID id,
        UUID departmentId,
        String departmentName,
        String name,
        String code,
        String degree,
        String branch,
        String description,
        Integer durationYears,
        Integer totalSemesters,
        Integer intake,
        Integer establishedYear,
        ProgramStatus status,
        CurrentAccreditation currentAccreditation,
        CurrentAcademicYear currentAcademicYear,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    /** Lightweight snapshot of the program's most relevant accreditation cycle. */
    public record CurrentAccreditation(UUID id, String name, String tier, String status) {
    }

    /** Lightweight snapshot of the program's active academic year. */
    public record CurrentAcademicYear(UUID id, String name, String status) {
    }
}
