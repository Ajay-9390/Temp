package com.nba.attainment.exception;

import java.util.UUID;

/**
 * Thrown when a requested PO/PSO attainment record does not exist.
 */
public class AttainmentNotFoundException extends RuntimeException {

    public AttainmentNotFoundException(String message) {
        super(message);
    }

    public static AttainmentNotFoundException forPO(UUID poId, String academicYear) {
        return new AttainmentNotFoundException(
                "No PO attainment found for poId=" + poId + ", academicYear=" + academicYear);
    }

    public static AttainmentNotFoundException forPSO(UUID psoId, String academicYear) {
        return new AttainmentNotFoundException(
                "No PSO attainment found for psoId=" + psoId + ", academicYear=" + academicYear);
    }
}
