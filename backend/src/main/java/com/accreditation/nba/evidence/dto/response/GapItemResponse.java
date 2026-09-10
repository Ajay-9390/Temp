package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import java.util.UUID;

/**
 * One required-evidence line in a gap report. {@code currentStatus} is either
 * {@code "MISSING"} or the best matching evidence status; {@code satisfied} is true when
 * approved evidence exists for the requirement.
 */
public record GapItemResponse(
        UUID criterionId,
        UUID requirementId,
        String criterionCode,
        String requirementCode,
        EvidenceCategory expectedCategory,
        String expectedEvidence,
        boolean mandatory,
        String currentStatus,
        UUID evidenceId,
        boolean satisfied
) {
}
