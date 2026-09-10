package com.example.nba.academic;

import java.util.EnumSet;
import java.util.Set;

/**
 * Shared lifecycle for AcademicYear and Semester.
 * <pre>
 * PLANNED   → ACTIVE
 * ACTIVE    → COMPLETED
 * COMPLETED → ARCHIVED
 * ARCHIVED  → COMPLETED   (restore / unarchive)
 * </pre>
 */
public enum AcademicLifecycleStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    ARCHIVED;

    public Set<AcademicLifecycleStatus> allowedNext() {
        return switch (this) {
            case PLANNED -> EnumSet.of(ACTIVE);
            case ACTIVE -> EnumSet.of(COMPLETED);
            case COMPLETED -> EnumSet.of(ARCHIVED);
            // ARCHIVED is reversible: restore to COMPLETED.
            case ARCHIVED -> EnumSet.of(COMPLETED);
        };
    }

    public boolean canTransitionTo(AcademicLifecycleStatus target) {
        return allowedNext().contains(target);
    }
}
