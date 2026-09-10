package com.example.nba.accreditation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Externalized accreditation metadata. Tiers are configurable (not hardcoded) so additional
 * tiers can be introduced without code changes — set {@code app.accreditation.allowed-tiers}.
 * No NBA scoring/criteria rules live here.
 */
@ConfigurationProperties(prefix = "app.accreditation")
public record AccreditationProperties(
        List<String> allowedTiers,
        boolean allowMultipleActiveCycles
) {
    public AccreditationProperties {
        if (allowedTiers == null || allowedTiers.isEmpty()) {
            allowedTiers = List.of("TIER_I", "TIER_II");
        }
    }
}
