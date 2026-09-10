package com.accreditation.nba.evidence.exception;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends EvidenceException {

    public InvalidStateTransitionException(EvidenceStatus from, EvidenceStatus to) {
        super(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION",
                "Cannot transition evidence from " + from + " to " + to);
    }

    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", message);
    }
}
