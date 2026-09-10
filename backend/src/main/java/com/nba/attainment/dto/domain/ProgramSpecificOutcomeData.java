package com.nba.attainment.dto.domain;

import java.util.UUID;

/**
 * Domain contract for a Program Specific Outcome (PSO).
 *
 * <p>Owned by the PO/PSO Management module. This module consumes it
 * via {@code ProgramSpecificOutcomeProvider} without coupling to their JPA entities.
 *
 * @param psoId       Unique identifier
 * @param programId   Program this PSO belongs to
 * @param code        Short code, e.g. "PSO1"
 * @param description Full description of the outcome
 */
public record ProgramSpecificOutcomeData(
        UUID   psoId,
        UUID   programId,
        String code,
        String description
) {

    public ProgramSpecificOutcomeData {
        if (psoId       == null) throw new IllegalArgumentException("psoId must not be null");
        if (programId   == null) throw new IllegalArgumentException("programId must not be null");
        if (code        == null || code.isBlank()) throw new IllegalArgumentException("code must not be blank");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description must not be blank");
    }
}
