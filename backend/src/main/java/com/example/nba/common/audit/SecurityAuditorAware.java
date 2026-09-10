package com.example.nba.common.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Supplies the current principal (Keycloak subject / username) for {@code createdBy} and
 * {@code updatedBy}. Falls back to {@code system} when unauthenticated (e.g. seed loader).
 */
@Component("auditorProvider")
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("system");
        }
        return Optional.of(auth.getName());
    }
}
