package com.accreditation.nba.evidence.repository.view;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import java.util.UUID;

/**
 * Projection linking a criterion/requirement to a piece of evidence and its status.
 * Used by gap detection to determine coverage of required evidence.
 */
public interface RequirementCoverageView {
    UUID getCriterionId();

    UUID getRequirementId();

    UUID getEvidenceId();

    EvidenceStatus getStatus();
}
