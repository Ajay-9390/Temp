package com.nba.attainment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration properties bound from {@code application.yml}
 * under the {@code attainment} prefix.
 */
@ConfigurationProperties(prefix = "attainment")
public record AttainmentProperties(
        Calculation calculation,
        Thresholds  thresholds
) {

    public record Calculation(
            String defaultMethod,
            String defaultVersion
    ) {
        public Calculation {
            defaultMethod  = defaultMethod  != null ? defaultMethod  : "WEIGHTED";
            defaultVersion = defaultVersion != null ? defaultVersion : "v1";
        }
    }

    /**
     * Attainment level thresholds for status labelling in the UI.
     * Values represent minimum percentage to reach that level.
     *
     * <ul>
     *   <li>≥ excellent  → "Excellent"</li>
     *   <li>≥ good       → "Good"</li>
     *   <li>≥ 0          → "Needs Improvement"</li>
     * </ul>
     */
    public record Thresholds(
            double excellent,
            double good,
            double needsImprovement
    ) {
        public Thresholds {
            if (excellent < 0 || excellent > 100)
                throw new IllegalArgumentException("excellent threshold must be in [0, 100]");
            if (good < 0 || good > 100)
                throw new IllegalArgumentException("good threshold must be in [0, 100]");
        }

        public String statusFor(double attainment) {
            if (attainment >= excellent)       return "Excellent";
            if (attainment >= good)            return "Good";
            return "Needs Improvement";
        }
    }
}
