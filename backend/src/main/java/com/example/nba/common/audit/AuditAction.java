package com.example.nba.common.audit;

/** Business-level audit actions tracked in the custom audit log. */
public enum AuditAction {
    CREATE,
    UPDATE,
    STATUS_CHANGE,
    DELETE_ATTEMPT,
    ARCHIVE
}
