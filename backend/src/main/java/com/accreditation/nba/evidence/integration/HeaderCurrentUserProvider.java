package com.accreditation.nba.evidence.integration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Mock identity provider for the no-auth development phase. Reads optional {@code X-User-Id}
 * and {@code X-User-Name} request headers so the frontend can simulate different users;
 * falls back to {@code mock-user} when absent (or outside a request, e.g. async threads).
 */
@Component
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    public static final String DEFAULT_USER = "mock-user";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";

    @Override
    public String currentUserId() {
        return headerOrDefault(USER_ID_HEADER, DEFAULT_USER);
    }

    @Override
    public String currentUserName() {
        return headerOrDefault(USER_NAME_HEADER, currentUserId());
    }

    private String headerOrDefault(String header, String fallback) {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return fallback;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }
}
