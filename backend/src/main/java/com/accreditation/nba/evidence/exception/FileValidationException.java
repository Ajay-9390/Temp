package com.accreditation.nba.evidence.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when an uploaded file fails validation. The HTTP status varies with the failure
 * kind (unsupported type &rarr; 415, too large &rarr; 413, empty/corrupt/mismatch &rarr; 422).
 */
public class FileValidationException extends EvidenceException {

    public FileValidationException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static FileValidationException empty() {
        return new FileValidationException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_FILE",
                "Uploaded file is empty");
    }

    public static FileValidationException tooLarge(long size, long max) {
        return new FileValidationException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "File size " + size + " bytes exceeds the maximum of " + max + " bytes");
    }

    public static FileValidationException unsupportedExtension(String ext) {
        return new FileValidationException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE",
                "File extension not allowed: " + ext);
    }

    public static FileValidationException contentMismatch(String ext, String detected) {
        return new FileValidationException(HttpStatus.UNPROCESSABLE_ENTITY, "FILE_CONTENT_MISMATCH",
                "File content (" + detected + ") does not match its extension (." + ext + ")");
    }

    public static FileValidationException unsafeName() {
        return new FileValidationException(HttpStatus.UNPROCESSABLE_ENTITY, "UNSAFE_FILE_NAME",
                "File name is unsafe or invalid");
    }
}
