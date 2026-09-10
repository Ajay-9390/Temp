package com.example.nba.program.dto;

import com.example.nba.program.entity.ProgramStatus;
import jakarta.validation.constraints.NotNull;

public record ProgramStatusRequest(
        @NotNull(message = "status is required") ProgramStatus status
) {
}
