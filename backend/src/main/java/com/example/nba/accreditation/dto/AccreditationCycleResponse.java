package com.example.nba.accreditation.dto;

import com.example.nba.accreditation.entity.AccreditationCycleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccreditationCycleResponse(
        UUID id,
        UUID programId,
        String name,
        String tier,
        String frameworkVersion,
        Integer applicationYear,
        LocalDate startDate,
        LocalDate endDate,
        AccreditationCycleStatus status,
        String remarks,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
