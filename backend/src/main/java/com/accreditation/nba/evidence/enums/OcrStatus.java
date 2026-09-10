package com.accreditation.nba.evidence.enums;

/**
 * State of asynchronous text extraction / OCR for a stored file version.
 */
public enum OcrStatus {
    /** Queued for background processing. */
    PENDING,
    /** Currently being processed. */
    PROCESSING,
    /** Text/metadata extracted successfully. */
    COMPLETED,
    /** Processing failed; file remains usable, extraction can be retried. */
    FAILED,
    /** Not applicable (e.g. text already extracted, or OCR disabled). */
    SKIPPED
}
