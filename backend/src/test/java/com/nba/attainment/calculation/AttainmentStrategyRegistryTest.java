package com.nba.attainment.calculation;

import com.nba.attainment.exception.CalculationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AttainmentStrategyRegistryTest {

    @Test
    void resolvesRegisteredStrategy() {
        AttainmentStrategyRegistry registry =
                new AttainmentStrategyRegistry(List.of(new WeightedCOAttainmentStrategy()));

        AttainmentCalculationStrategy resolved = registry.resolve("WEIGHTED");

        assertThat(resolved).isInstanceOf(WeightedCOAttainmentStrategy.class);
    }

    @Test
    void resolveCaseInsensitive() {
        AttainmentStrategyRegistry registry =
                new AttainmentStrategyRegistry(List.of(new WeightedCOAttainmentStrategy()));

        assertThat(registry.resolve("weighted")).isNotNull();
        assertThat(registry.resolve("Weighted")).isNotNull();
    }

    @Test
    void unknownStrategy_throwsCalculationException() {
        AttainmentStrategyRegistry registry =
                new AttainmentStrategyRegistry(List.of(new WeightedCOAttainmentStrategy()));

        assertThatThrownBy(() -> registry.resolve("NONEXISTENT"))
                .isInstanceOf(CalculationException.class)
                .hasMessageContaining("Unsupported calculation method");
    }

    @Test
    void duplicateStrategy_throwsIllegalState() {
        assertThatThrownBy(() ->
                new AttainmentStrategyRegistry(List.of(
                        new WeightedCOAttainmentStrategy(),
                        new WeightedCOAttainmentStrategy()
                )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void availableStrategies_containsWeighted() {
        AttainmentStrategyRegistry registry =
                new AttainmentStrategyRegistry(List.of(new WeightedCOAttainmentStrategy()));

        assertThat(registry.availableStrategies()).contains("WEIGHTED");
    }
}
