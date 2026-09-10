package com.nba.attainment.exception;

/**
 * Thrown when attainment data fails domain-level validation.
 * Examples: attainment outside [0, 100], mapping level outside [0, 3],
 * missing CO attainment, conflicting programId.
 */
public class InvalidAttainmentException extends RuntimeException {

    public InvalidAttainmentException(String message) {
        super(message);
    }

    public InvalidAttainmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
