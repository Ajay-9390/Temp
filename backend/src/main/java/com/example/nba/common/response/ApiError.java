package com.example.nba.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope.
 *
 * <pre>
 * { "success": false,
 *   "error": { "code": "PROGRAM_CODE_ALREADY_EXISTS", "message": "...", "details": [...] },
 *   "timestamp": "..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        boolean success,
        ErrorBody error,
        Instant timestamp
) {
    public static ApiError of(String code, String message, List<FieldViolation> details) {
        return new ApiError(false, new ErrorBody(code, message, details), Instant.now());
    }

    public static ApiError of(String code, String message) {
        return of(code, message, List.of());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorBody(String code, String message, List<FieldViolation> details) {
    }

    public record FieldViolation(String field, String message) {
    }
}
