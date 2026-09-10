package com.example.nba.department.dto;

import com.example.nba.department.entity.DepartmentStatus;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        UUID institutionId,
        String name,
        String code,
        String description,
        String hodUserId,
        DepartmentStatus status,
        long programCount,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
