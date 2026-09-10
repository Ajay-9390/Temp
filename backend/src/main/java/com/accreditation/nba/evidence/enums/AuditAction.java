package com.accreditation.nba.evidence.enums;

/**
 * Business-level audit events tracked for evidence. Scoped to this module only; the
 * platform-wide audit module (owned by another team) is out of scope.
 */
public enum AuditAction {
    EVIDENCE_CREATED,
    EVIDENCE_UPDATED,
    EVIDENCE_SUBMITTED,
    EVIDENCE_REVIEWED,
    EVIDENCE_APPROVED,
    EVIDENCE_REJECTED,
    EVIDENCE_CHANGES_REQUESTED,
    EVIDENCE_VERSION_CREATED,
    EVIDENCE_MAPPED,
    EVIDENCE_UNMAPPED,
    EVIDENCE_ARCHIVED,
    EVIDENCE_DELETED
}
