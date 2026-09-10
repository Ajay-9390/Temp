package com.nba.attainment.config;

import com.nba.attainment.calculation.AttainmentCalculationStrategy;
import com.nba.attainment.calculation.AttainmentStrategyRegistry;
import com.nba.attainment.calculation.POAttainmentCalculator;
import com.nba.attainment.calculation.PSOAttainmentCalculator;
import com.nba.attainment.calculation.WeightedCOAttainmentStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration for the calculation layer.
 *
 * <p>This is the ONLY place in the calculation package that touches Spring.
 * The strategy and calculator classes are plain Java objects — no {@code @Component}
 * annotations on them — keeping them fully testable without a Spring context.
 *
 * <p>To add a new strategy:
 * <ol>
 *   <li>Create a class implementing {@link AttainmentCalculationStrategy}</li>
 *   <li>Add a {@code @Bean} method here</li>
 *   <li>Add it to the {@code allStrategies} list</li>
 * </ol>
 */
@Configuration
public class AttainmentCalculationConfig {

    // ── Strategy beans ────────────────────────────────────────────────────────

    @Bean
    public WeightedCOAttainmentStrategy weightedCOAttainmentStrategy() {
        return new WeightedCOAttainmentStrategy();
    }

    // ── Registry ─────────────────────────────────────────────────────────────

    @Bean
    public AttainmentStrategyRegistry attainmentStrategyRegistry(
            List<AttainmentCalculationStrategy> allStrategies) {
        return new AttainmentStrategyRegistry(allStrategies);
    }

    // ── Calculators ───────────────────────────────────────────────────────────

    /**
     * PO calculator using the default weighted strategy.
     * The application service swaps the strategy at runtime via the registry.
     */
    @Bean
    public POAttainmentCalculator poAttainmentCalculator(
            WeightedCOAttainmentStrategy weightedStrategy) {
        return new POAttainmentCalculator(weightedStrategy);
    }

    /**
     * PSO calculator using the default weighted strategy.
     */
    @Bean
    public PSOAttainmentCalculator psoAttainmentCalculator(
            WeightedCOAttainmentStrategy weightedStrategy) {
        return new PSOAttainmentCalculator(weightedStrategy);
    }
}
