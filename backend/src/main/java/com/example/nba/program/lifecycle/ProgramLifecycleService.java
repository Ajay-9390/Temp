package com.example.nba.program.lifecycle;

import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.program.entity.ProgramStatus;
import org.springframework.stereotype.Service;

/**
 * Custom state-transition service for {@link ProgramStatus}. Kept intentionally simple and
 * self-contained (no Camunda). Designed so the transition rules can later be externalized to
 * a workflow engine without changing callers.
 */
@Service
public class ProgramLifecycleService {

    public void validateTransition(ProgramStatus current, ProgramStatus target) {
        if (current == target) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Program is already in status " + current);
        }
        if (!current.canTransitionTo(target)) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Invalid program status transition: " + current + " -> " + target
                            + ". Allowed: " + current.allowedNext());
        }
    }
}
