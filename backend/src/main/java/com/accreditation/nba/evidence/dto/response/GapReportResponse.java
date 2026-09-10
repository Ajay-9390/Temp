package com.accreditation.nba.evidence.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Gap analysis for a program + academic year: how much required evidence exists, is
 * approved, pending, rejected or missing.
 */
public record GapReportResponse(
        UUID programId,
        UUID academicYearId,
        long totalRequired,
        long uploaded,
        long approved,
        long pending,
        long rejected,
        long missing,
        List<GapItemResponse> items
) {
}
