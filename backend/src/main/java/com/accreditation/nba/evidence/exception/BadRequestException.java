package com.accreditation.nba.evidence.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends EvidenceException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }
}
