package com.example.nba.common.exception;

import lombok.Getter;

/**
 * Base application exception carrying a stable {@link ErrorCode}.
 * Prefer the dedicated subclasses for common cases.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
