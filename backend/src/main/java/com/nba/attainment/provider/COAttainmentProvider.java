package com.nba.attainment.provider;

import com.nba.attainment.dto.domain.COAttainmentData;

import java.util.List;
import java.util.UUID;

/**
 * Contract for obtaining CO attainment data.
 *
 * <p>This module does NOT calculate CO attainment — that responsibility
 * belongs to the CO Attainment module. This interface is the seam
 * through which this module receives already-calculated CO attainment.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code MockCOAttainmentProvider} — used during development</li>
 *   <li>{@code RealCOAttainmentProvider} — wired when the CO Attainment
 *       module is integrated (calls their service or DB via an adapter)</li>
 * </ul>
 */
public interface COAttainmentProvider {

    /**
     * Returns all CO attainment records for the given program and academic year.
     *
     * @param programId    the program to retrieve CO attainment for
     * @param academicYear e.g. "2023-24"
     * @return list of CO attainment data; never null, may be empty
     */
    List<COAttainmentData> getAttainments(UUID programId, String academicYear);

    /**
     * Returns CO attainment for a specific course within a program and year.
     *
     * @param programId    the program
     * @param courseId     the specific course
     * @param academicYear e.g. "2023-24"
     * @return list of CO attainment data for that course
     */
    List<COAttainmentData> getAttainmentsForCourse(UUID programId, UUID courseId, String academicYear);
}
