package com.example.nba.common.exception;

/** Thrown on uniqueness violations or invalid state transitions. Maps to HTTP 409. */
public class ConflictException extends ApiException {
    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
