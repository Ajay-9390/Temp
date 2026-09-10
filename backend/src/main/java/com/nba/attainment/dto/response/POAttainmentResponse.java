package com.nba.attainment.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * API response for a single PO attainment record.
 */
public record POAttainmentResponse(
        UUID    id,
        UUID    programId,
        UUID    poId,
        String  poCode,
        String  poDescription,
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
