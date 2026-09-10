package com.nba.attainment.dto.domain;

import java.util.UUID;

/**
 * Domain contract for a Program Outcome (PO).
 *
 * <p>Owned by the PO/PSO Management module. This module consumes it
 * via {@code ProgramOutcomeProvider} without coupling to their JPA entities.
 *
 * @param poId        Unique identifier
 * @param programId   Program this PO belongs to
 * @param code        Short code, e.g. "PO1"
 * @param description Full description of the outcome
 */
public record ProgramOutcomeData(
        UUID   poId,
        UUID   programId,
        String code,
        String description
) {

    public ProgramOutcomeData {
        if (poId        == null) throw new IllegalArgumentException("poId must not be null");
        if (programId   == null) throw new IllegalArgumentException("programId must not be null");
        if (code        == null || code.isBlank()) throw new IllegalArgumentException("code must not be blank");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description must not be blank");
    }
}
