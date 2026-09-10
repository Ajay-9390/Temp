package com.example.nba.accreditation.dto;

import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import jakarta.validation.constraints.NotNull;

public record AccreditationCycleStatusRequest(
        @NotNull(message = "status is required") AccreditationCycleStatus status,
        String remarks
) {
}
