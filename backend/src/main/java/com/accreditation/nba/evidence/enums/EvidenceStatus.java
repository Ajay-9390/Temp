package com.accreditation.nba.evidence.enums;

/**
 * Lifecycle states of an evidence item. Valid transitions between states are enforced by
 * {@code com.accreditation.nba.evidence.workflow.EvidenceWorkflow} (service layer), never
 * by controllers.
 */
public enum EvidenceStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    CHANGES_REQUIRED,
    APPROVED,
    REJECTED,
    ARCHIVED;

    /** ARCHIVED is terminal. */
    public boolean isTerminal() {
        return this == ARCHIVED;
    }

    /** Whether metadata edits are permitted in this state. */
    public boolean isEditable() {
        return this == DRAFT || this == CHANGES_REQUIRED;
    }
}
