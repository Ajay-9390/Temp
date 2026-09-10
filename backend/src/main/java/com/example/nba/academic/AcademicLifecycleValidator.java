package com.example.nba.academic;

import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

/** Shared state-transition validator for AcademicYear and Semester. */
@Service
public class AcademicLifecycleValidator {

    public void validateTransition(AcademicLifecycleStatus current, AcademicLifecycleStatus target) {
        if (current == target) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Already in status " + current);
        }
        if (!current.canTransitionTo(target)) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Invalid status transition: " + current + " -> " + target
                            + ". Allowed: " + current.allowedNext());
        }
    }
}
