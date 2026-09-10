package com.accreditation.nba.evidence.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends EvidenceException {

    public StorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message);
    }

    public StorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message, cause);
    }
}
