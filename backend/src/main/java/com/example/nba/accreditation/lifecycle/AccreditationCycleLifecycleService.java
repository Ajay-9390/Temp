package com.example.nba.accreditation.lifecycle;

import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Custom state-transition service for accreditation cycles (no Camunda). The transition
 * matrix lives in {@link AccreditationCycleStatus}; this service enforces it.
 */
@Service
public class AccreditationCycleLifecycleService {

    public void validateTransition(AccreditationCycleStatus current, AccreditationCycleStatus target) {
        if (current == target) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Accreditation cycle is already in status " + current);
        }
        if (!current.canTransitionTo(target)) {
            throw new ConflictException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Invalid accreditation cycle transition: " + current + " -> " + target
                            + ". Allowed: " + current.allowedNext());
        }
    }
}
