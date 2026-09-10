package com.nba.attainment.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * API response for a single PSO attainment record.
 */
public record PSOAttainmentResponse(
        UUID    id,
        UUID    programId,
        UUID    psoId,
        String  psoCode,
        String  psoDescription,
        String  academicYear,
        Double  directAttainment,
        Double  indirectAttainment,
        Double  finalAttainment,
        String  calculationMethod,
        String  calculationVersion,
        String  status,
        Instant createdAt,
        Instant updatedAt
) {}
