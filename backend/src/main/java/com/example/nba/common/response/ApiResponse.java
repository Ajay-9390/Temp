package com.example.nba.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard success envelope returned by every controller.
 *
 * <pre>
 * { "success": true, "data": {...}, "message": "...", "timestamp": "..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, null);
    }
}
