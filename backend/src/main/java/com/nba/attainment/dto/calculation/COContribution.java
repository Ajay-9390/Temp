package com.nba.attainment.dto.calculation;

import java.util.UUID;

/**
 * Represents a single CO's contribution to a PO/PSO calculation.
 *
 * <p>Pure data carrier — no Spring, no JPA, no I/O.
 *
 * @param coId         CO identifier
 * @param coCode       Short CO code (e.g. "CO1")
 * @param courseId     Course this CO belongs to
 * @param courseName   Human-readable course name
 * @param attainment   CO attainment percentage [0, 100]
 * @param mappingLevel Correlation level [0, 3]
 */
public record COContribution(
        UUID   coId,
        String coCode,
        UUID   courseId,
        String courseName,
        double attainment,
        int    mappingLevel
) {}
