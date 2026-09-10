package com.nba.attainment.dto.calculation;

import java.util.List;
import java.util.UUID;

/**
 * Result produced by a calculator for a single PO or PSO.
 *
 * <p>Contains the numeric attainment value plus a full trace so every
 * number can be explained back to the user.
 *
 * @param outcomeId          PO or PSO UUID
 * @param outcomeCode        Short code (e.g. "PO1")
 * @param programId          Owning program
 * @param academicYear       Academic year
 * @param directAttainment   Calculated direct attainment [0, 100]
 * @param indirectAttainment Indirect attainment — null until indirect calculation is wired in
 * @param finalAttainment    Final combined attainment (currently same as direct)
 * @param strategyName       Strategy that produced this result
 * @param calculationVersion Version string (e.g. "v1")
 * @param trace              Step-by-step calculation trace for drill-down
 */
public record AttainmentCalculationResult(
        UUID                   outcomeId,
        String                 outcomeCode,
        UUID                   programId,
        String                 academicYear,
        double                 directAttainment,
        Double                 indirectAttainment,
        double                 finalAttainment,
        String                 strategyName,
        String                 calculationVersion,
        CalculationTrace       trace
) {

    /** Minimal factory when only direct attainment is available. */
    public static AttainmentCalculationResult directOnly(
            UUID outcomeId, String outcomeCode, UUID programId, String academicYear,
            double directAttainment, String strategyName, String version,
            CalculationTrace trace) {
        return new AttainmentCalculationResult(
                outcomeId, outcomeCode, programId, academicYear,
                directAttainment, null, directAttainment,
                strategyName, version, trace);
    }

    // ----------------------------------------------------------------
    // Nested trace model
    // ----------------------------------------------------------------

    /**
     * Full drill-down trace for one PO/PSO.
     *
     * @param formula         Human-readable formula description
     * @param courseBreakdown Per-course contribution details
     * @param totalWeight     Sum of all mapping levels
     * @param weightedSum     Sum of (attainment × mapping) across all COs
     * @param computedValue   Final computed value (= weightedSum / totalWeight)
     */
    public record CalculationTrace(
            String                    formula,
            List<CourseContribution>  courseBreakdown,
            double                    totalWeight,
            double                    weightedSum,
            double                    computedValue
    ) {
        public CalculationTrace {
            if (courseBreakdown == null) courseBreakdown = List.of();
        }
    }

    /**
     * Contribution of a single course to a PO/PSO.
     *
     * @param courseId        Course UUID
     * @param courseName      Human-readable name
     * @param coContributions Each CO's individual contribution
     */
    public record CourseContribution(
            UUID                  courseId,
            String                courseName,
            List<COContributionDetail> coContributions
    ) {}

    /**
     * Contribution of a single CO.
     *
     * @param coId            CO UUID
     * @param coCode          Short code
     * @param attainment      CO attainment %
     * @param mappingLevel    Mapping level [0, 3]
     * @param weightedValue   attainment × mappingLevel
     */
    public record COContributionDetail(
            UUID   coId,
            String coCode,
            double attainment,
            int    mappingLevel,
            double weightedValue
    ) {}
}
