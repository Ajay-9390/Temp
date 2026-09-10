package com.nba.attainment.dto.domain;

import java.util.UUID;

/**
 * Domain contract representing CO attainment for a single course outcome
 * within a specific course for an academic year.
 *
 * <p>This is the module boundary data contract. Other modules provide data
 * in this shape via {@code COAttainmentProvider} implementations.
 *
 * <p>Attainment is expressed as a percentage [0.0, 100.0].
 *
 * @param programId    The program this CO belongs to
 * @param courseId     The course this CO belongs to
 * @param courseName   Human-readable course name (for traces / UI)
 * @param coId         The unique identifier of this Course Outcome
 * @param coCode       Short code, e.g. "CO1"
 * @param academicYear e.g. "2023-24"
 * @param attainment   Attainment percentage [0, 100]
 */
public record COAttainmentData(
        UUID   programId,
        UUID   courseId,
        String courseName,
        UUID   coId,
        String coCode,
        String academicYear,
        double attainment
) {

    public COAttainmentData {
        if (programId    == null) throw new IllegalArgumentException("programId must not be null");
        if (courseId     == null) throw new IllegalArgumentException("courseId must not be null");
        if (coId         == null) throw new IllegalArgumentException("coId must not be null");
        if (coCode       == null || coCode.isBlank()) throw new IllegalArgumentException("coCode must not be blank");
        if (academicYear == null || academicYear.isBlank()) throw new IllegalArgumentException("academicYear must not be blank");
        if (attainment < 0.0 || attainment > 100.0)
            throw new IllegalArgumentException("attainment must be in [0, 100], got: " + attainment);
    }
}
