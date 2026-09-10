package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationInput;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;

/**
 * Strategy contract for computing attainment from a prepared input bundle.
 *
 * <p><strong>Architectural contract:</strong> implementations of this
 * interface must be pure calculation objects. They must NOT:
 * <ul>
 *   <li>Access any Spring bean directly</li>
 *   <li>Call any JPA repository</li>
 *   <li>Make HTTP requests</li>
 *   <li>Read from any database</li>
 *   <li>Know about mock or real providers</li>
 * </ul>
 *
 * <p>This contract enables the calculation engine to be tested in isolation
 * with plain JUnit — no Spring context, no PostgreSQL.
 *
 * <p>New strategies (e.g. {@code ThresholdBasedStrategy},
 * {@code NormalizedStrategy}) can be added without touching any other
 * layer. The {@link AttainmentStrategyRegistry} maps strategy names to
 * instances at runtime.
 */
public interface AttainmentCalculationStrategy {

    /**
     * The unique name key used to look up this strategy.
     * Must match the value supplied in {@code calculationMethod} on requests.
     * Example: {@code "WEIGHTED"}.
     */
    String strategyName();

    /**
     * Computes attainment for a single PO or PSO given the prepared input.
     *
     * @param input fully prepared bundle of CO contributions and metadata
     * @return calculation result with numeric attainment and full trace
     * @throws com.nba.attainment.exception.CalculationException if the
     *         input is invalid or the calculation cannot proceed
     */
    AttainmentCalculationResult calculate(AttainmentCalculationInput input);
}
