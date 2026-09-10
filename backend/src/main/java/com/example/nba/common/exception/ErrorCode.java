package com.example.nba.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Stable machine-readable error codes surfaced to API clients.
 * The {@code code} string is part of the integration contract and must not change casually.
 */
public enum ErrorCode {

    // Generic
    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    // Domain: Institution
    INSTITUTION_NOT_FOUND(HttpStatus.NOT_FOUND),
    INSTITUTION_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Domain: Department
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    DEPARTMENT_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Domain: Program
    PROGRAM_NOT_FOUND(HttpStatus.NOT_FOUND),
    PROGRAM_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Domain: Accreditation Cycle
    ACCREDITATION_CYCLE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ACTIVE_ACCREDITATION_CYCLE_EXISTS(HttpStatus.CONFLICT),

    // Domain: Academic Year
    ACADEMIC_YEAR_NOT_FOUND(HttpStatus.NOT_FOUND),
    ACADEMIC_YEAR_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Domain: Semester
    SEMESTER_NOT_FOUND(HttpStatus.NOT_FOUND),
    SEMESTER_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT),
    SEMESTER_DATES_OUT_OF_RANGE(HttpStatus.UNPROCESSABLE_ENTITY),

    // Lifecycle
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT),
    INVALID_DATE_RANGE(HttpStatus.UNPROCESSABLE_ENTITY),
    DATE_RANGE_OVERLAP(HttpStatus.CONFLICT),
    DELETE_NOT_ALLOWED(HttpStatus.CONFLICT);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
