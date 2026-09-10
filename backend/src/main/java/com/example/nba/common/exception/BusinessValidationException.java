package com.example.nba.common.exception;

/** Thrown when business/validation rules fail (e.g. bad date range). Maps to HTTP 422. */
public class BusinessValidationException extends ApiException {
    public BusinessValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
