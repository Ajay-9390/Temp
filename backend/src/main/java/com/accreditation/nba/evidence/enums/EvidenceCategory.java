package com.accreditation.nba.evidence.enums;

/**
 * Configurable evidence categories. Behaviour must not be hard-coded against specific
 * values throughout the application; treat this as master data. New categories can be
 * added here (or later externalised to a master-data table) without changing business logic.
 */
public enum EvidenceCategory {
    COURSE_FILE,
    ASSESSMENT,
    FACULTY,
    STUDENT,
    LABORATORY,
    INFRASTRUCTURE,
    PLACEMENT,
    FEEDBACK,
    RESEARCH,
    TRAINING,
    ACADEMIC,
    ADMINISTRATIVE,
    OTHER
}
