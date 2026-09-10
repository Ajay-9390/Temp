package com.nba.attainment.dto.calculation;

import java.util.List;
import java.util.UUID;

/**
 * Complete input bundle fed to a {@code POAttainmentCalculator} or
 * {@code PSOAttainmentCalculator}.
 *
 * <p>Pure data — no Spring, no JPA, no I/O. The calculators receive this
 * object and produce an {@link AttainmentCalculationResult}.
 *
 * @param outcomeId      PO or PSO UUID being calculated
 * @param outcomeCode    Short code (e.g. "PO1", "PSO2")
 * @param programId      Program this outcome belongs to
 * @param academicYear   Academic year (e.g. "2023-24")
 * @param contributions  All CO contributions mapped to this outcome
 * @param strategyName   Name of the calculation strategy to apply
 */
public record AttainmentCalculationInput(
        UUID                  outcomeId,
        String                outcomeCode,
        UUID                  programId,
        String                academicYear,
        List<COContribution>  contributions,
        String                strategyName
) {

    public AttainmentCalculationInput {
        if (outcomeId    == null) throw new IllegalArgumentException("outcomeId must not be null");
        if (outcomeCode  == null || outcomeCode.isBlank()) throw new IllegalArgumentException("outcomeCode must not be blank");
        if (programId    == null) throw new IllegalArgumentException("programId must not be null");
        if (academicYear == null || academicYear.isBlank()) throw new IllegalArgumentException("academicYear must not be blank");
        if (contributions == null) throw new IllegalArgumentException("contributions must not be null");
        contributions = List.copyOf(contributions); // defensive copy
    }

    public boolean hasContributions() {
        return !contributions.isEmpty();
    }
}
