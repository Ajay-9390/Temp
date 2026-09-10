package com.nba.attainment.calculation;

import com.nba.attainment.exception.CalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry that maps strategy name keys to {@link AttainmentCalculationStrategy}
 * instances.
 *
 * <p>This is an application-level component (annotated with {@code @Component}
 * in {@link AttainmentCalculationConfig}). It is the only class in the
 * calculation package that touches Spring — purely for bean wiring.
 * The strategies themselves remain Spring-free.
 *
 * <p>To add a new strategy:
 * <ol>
 *   <li>Implement {@link AttainmentCalculationStrategy}</li>
 *   <li>Register it in {@link AttainmentCalculationConfig}</li>
 * </ol>
 * No other code changes are required.
 */
public class AttainmentStrategyRegistry {

    private static final Logger log = LoggerFactory.getLogger(AttainmentStrategyRegistry.class);

    private final Map<String, AttainmentCalculationStrategy> strategies;

    public AttainmentStrategyRegistry(Collection<AttainmentCalculationStrategy> strategies) {
        Map<String, AttainmentCalculationStrategy> map = new HashMap<>();
        for (AttainmentCalculationStrategy s : strategies) {
            String key = s.strategyName().toUpperCase();
            if (map.containsKey(key)) {
                throw new IllegalStateException(
                        "Duplicate calculation strategy registered: " + key);
            }
            map.put(key, s);
            log.info("Registered attainment strategy: {}", key);
        }
        this.strategies = Collections.unmodifiableMap(map);
    }

    /**
     * Resolves a strategy by name (case-insensitive).
     *
     * @param name e.g. "WEIGHTED"
     * @return the strategy instance
     * @throws CalculationException if no strategy is registered for that name
     */
    public AttainmentCalculationStrategy resolve(String name) {
        AttainmentCalculationStrategy strategy = strategies.get(name.toUpperCase());
        if (strategy == null) {
            throw new CalculationException(
                    "Unsupported calculation method: '" + name + "'. " +
                    "Available methods: " + strategies.keySet());
        }
        return strategy;
    }

    /** Returns all registered strategy names. */
    public Collection<String> availableStrategies() {
        return strategies.keySet();
    }
}
