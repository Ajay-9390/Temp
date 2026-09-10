package com.accreditation.nba.evidence.report;

/**
 * A generated report ready to be returned as a download.
 */
public record ReportFile(
        String fileName,
        String contentType,
        byte[] content
) {
}
