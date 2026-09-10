package com.nba.attainment.exception;

/**
 * Thrown when the calculation engine cannot complete a calculation.
 * Examples: zero total weight, invalid input, unsupported strategy.
 *
 * <p>This exception is framework-independent — it may be thrown by
 * calculation classes that have no Spring/JPA dependencies.
 */
public class CalculationException extends RuntimeException {

    public CalculationException(String message) {
        super(message);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
