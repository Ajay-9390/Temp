package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationInput;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult.CalculationTrace;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult.COContributionDetail;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult.CourseContribution;
import com.nba.attainment.dto.calculation.COContribution;
import com.nba.attainment.exception.CalculationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Weighted CO attainment strategy — the primary NBA calculation method.
 *
 * <h2>Formula</h2>
 * <pre>
 *   Attainment(PO_k) =
 *       Σ [ COᵢ_attainment × COᵢ→PO_k_mappingLevel ]
 *       ─────────────────────────────────────────────
 *             Σ [ COᵢ→PO_k_mappingLevel ]
 * </pre>
 *
 * <p>COs with a mapping level of 0 are excluded from both numerator and
 * denominator (zero mapping = no contribution).
 *
 * <h2>Example</h2>
 * <pre>
 *   CO1 = 75%  mapping = 3  →  75 × 3 = 225
 *   CO2 = 68%  mapping = 2  →  68 × 2 = 136
 *   CO3 = 82%  mapping = 1  →  82 × 1 =  82
 *
 *   weightedSum  = 225 + 136 + 82 = 443
 *   totalWeight  = 3   +   2 +  1 =   6
 *   PO1 attainment = 443 / 6 = 73.83%
 * </pre>
 *
 * <p><strong>No Spring, JPA, or I/O dependencies.</strong>
 * Instantiate and test with plain {@code new WeightedCOAttainmentStrategy()}.
 */
public class WeightedCOAttainmentStrategy implements AttainmentCalculationStrategy {

    public static final String STRATEGY_NAME = "WEIGHTED";

    private static final String FORMULA_DESCRIPTION =
            "Weighted average: Σ(COᵢ_attainment × mappingLevel) / Σ(mappingLevel). " +
            "COs with mapping level 0 are excluded.";

    @Override
    public String strategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public AttainmentCalculationResult calculate(AttainmentCalculationInput input) {
        if (input == null) {
            throw new CalculationException("Calculation input must not be null");
        }

        // Filter out zero-mapping entries — they contribute nothing
        List<COContribution> active = input.contributions().stream()
                .filter(c -> c.mappingLevel() > 0)
                .toList();

        if (active.isEmpty()) {
            // No contributing COs — attainment is 0 by convention
            CalculationTrace emptyTrace = buildTrace(
                    List.of(), 0.0, 0.0, 0.0);
            return AttainmentCalculationResult.directOnly(
                    input.outcomeId(), input.outcomeCode(),
                    input.programId(), input.academicYear(),
                    0.0, STRATEGY_NAME, input.strategyName(), emptyTrace);
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (COContribution c : active) {
            weightedSum += c.attainment() * c.mappingLevel();
            totalWeight += c.mappingLevel();
        }

        if (totalWeight == 0.0) {
            // Defensive — covered by active.isEmpty() above, but guard division
            throw new CalculationException(
                    "Total mapping weight is zero for outcome " + input.outcomeCode() +
                    " — cannot divide. Ensure at least one CO has mapping level > 0.");
        }

        double directAttainment = round2dp(weightedSum / totalWeight);

        CalculationTrace trace = buildTrace(active, totalWeight, weightedSum, directAttainment);

        return AttainmentCalculationResult.directOnly(
                input.outcomeId(), input.outcomeCode(),
                input.programId(), input.academicYear(),
                directAttainment, STRATEGY_NAME, input.strategyName(), trace);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private CalculationTrace buildTrace(List<COContribution> active,
                                        double totalWeight,
                                        double weightedSum,
                                        double computedValue) {
        // Group by course for the drill-down view
        Map<UUID, List<COContribution>> byCourse = new LinkedHashMap<>();
        for (COContribution c : active) {
            byCourse.computeIfAbsent(c.courseId(), k -> new ArrayList<>()).add(c);
        }

        List<CourseContribution> courseBreakdown = new ArrayList<>();
        for (Map.Entry<UUID, List<COContribution>> entry : byCourse.entrySet()) {
            UUID courseId   = entry.getKey();
            String courseName = entry.getValue().get(0).courseName();

            List<COContributionDetail> details = entry.getValue().stream()
                    .map(c -> new COContributionDetail(
                            c.coId(), c.coCode(),
                            c.attainment(), c.mappingLevel(),
                            round2dp(c.attainment() * c.mappingLevel())))
                    .toList();

            courseBreakdown.add(new CourseContribution(courseId, courseName, details));
        }

        return new CalculationTrace(
                FORMULA_DESCRIPTION,
                courseBreakdown,
                totalWeight,
                round2dp(weightedSum),
                computedValue);
    }

    /** Rounds to 2 decimal places using standard rounding. */
    static double round2dp(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
