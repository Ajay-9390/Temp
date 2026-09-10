package com.accreditation.nba.evidence.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class EvidenceNotFoundException extends EvidenceException {

    public EvidenceNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "EVIDENCE_NOT_FOUND", "Evidence not found: " + id);
    }

    public EvidenceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
