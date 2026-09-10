package com.accreditation.nba.evidence.integration;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import java.util.UUID;

/**
 * A piece of evidence required by an NBA criterion/requirement, as defined by the external
 * NBA Criteria &amp; SAR Management module. This module consumes these descriptors for gap
 * detection but never creates or edits criteria/requirements.
 */
public record RequiredEvidence(
        UUID criterionId,
        UUID requirementId,
        String criterionCode,
        String requirementCode,
        EvidenceCategory expectedCategory,
        String expectedEvidenceName,
        boolean mandatory
) {
}
