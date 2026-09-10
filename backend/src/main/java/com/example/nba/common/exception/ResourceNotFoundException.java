package com.example.nba.common.exception;

/** Thrown when an entity referenced by id cannot be found. Maps to HTTP 404. */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
