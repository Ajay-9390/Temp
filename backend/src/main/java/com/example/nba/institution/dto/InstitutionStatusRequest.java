package com.example.nba.institution.dto;

import com.example.nba.institution.entity.InstitutionStatus;
import jakarta.validation.constraints.NotNull;

public record InstitutionStatusRequest(
        @NotNull(message = "status is required") InstitutionStatus status
) {
}
