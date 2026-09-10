package com.accreditation.nba.evidence.exception;

import org.springframework.http.HttpStatus;

public class DuplicateMappingException extends EvidenceException {

    public DuplicateMappingException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_MAPPING", message);
    }
}
