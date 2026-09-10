package com.accreditation.nba.evidence.enums;

/**
 * A reviewer's decision on an evidence item. Each decision maps deterministically to a
 * resulting {@link EvidenceStatus} (from {@link EvidenceStatus#UNDER_REVIEW}).
 */
public enum ReviewDecision {
    APPROVE(EvidenceStatus.APPROVED, false),
    REJECT(EvidenceStatus.REJECTED, true),
    REQUEST_CHANGES(EvidenceStatus.CHANGES_REQUIRED, true);

    private final EvidenceStatus resultingStatus;
    private final boolean commentRequired;

    ReviewDecision(EvidenceStatus resultingStatus, boolean commentRequired) {
        this.resultingStatus = resultingStatus;
        this.commentRequired = commentRequired;
    }

    public EvidenceStatus resultingStatus() {
        return resultingStatus;
    }

    /** REJECT and REQUEST_CHANGES require reviewer comments. */
    public boolean isCommentRequired() {
        return commentRequired;
    }
}
