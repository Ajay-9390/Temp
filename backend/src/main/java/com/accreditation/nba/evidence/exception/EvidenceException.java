package com.accreditation.nba.evidence.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all domain exceptions in the module. Carries the HTTP status and a stable
 * machine-readable {@code code} that the global handler serialises into {@code ApiError}.
 */
public abstract class EvidenceException extends RuntimeException {

    private final transient HttpStatus status;
    private final String code;

    protected EvidenceException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    protected EvidenceException(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
