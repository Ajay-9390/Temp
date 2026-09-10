package com.example.nba.program.dto;

import com.example.nba.program.entity.ProgramStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight program dashboard/overview. Deliberately contains NO attainment/readiness
 * calculations — those belong to future modules. {@code integrationPoints} advertises which
 * future modules will provide which metrics.
 */
public record ProgramOverviewResponse(
        UUID programId,
        String programName,
        String programCode,
        ProgramStatus programStatus,
        UUID departmentId,
        String departmentName,
        UUID institutionId,
        String institutionName,
        ProgramResponse.CurrentAccreditation currentAccreditation,
        String tier,
        ProgramResponse.CurrentAcademicYear currentAcademicYear,
        int totalSemesters,
        int accreditationCycleCount,
        int academicYearCount,
        List<ProgramResponse.CurrentAccreditation> previousAccreditations,
        Map<String, String> integrationPoints
) {
    /** Placeholder integration points consumed by other NBA modules (values are informational). */
    public static Map<String, String> defaultIntegrationPoints() {
        return Map.of(
                "courses", "Coming from Course Module",
                "cos", "Coming from OBE Module",
                "pos", "Coming from OBE Module",
                "evidence", "Coming from Evidence Module",
                "sarCompletion", "Coming from SAR Module"
        );
    }
}
