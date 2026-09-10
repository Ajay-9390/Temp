package com.accreditation.nba.evidence.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Uniform error body returned by the global exception handler.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorItem> fieldErrors
) {

    public record FieldErrorItem(String field, String message) {
    }
}
