package com.example.nba.department.dto;

import com.example.nba.department.entity.DepartmentStatus;
import jakarta.validation.constraints.NotNull;

public record DepartmentStatusRequest(
        @NotNull(message = "status is required") DepartmentStatus status
) {
}
