package com.example.nba.program.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Program lifecycle states and their allowed transitions.
 * <pre>
 * DRAFT    → ACTIVE
 * ACTIVE   → INACTIVE
 * INACTIVE → ACTIVE, ARCHIVED
 * ARCHIVED → INACTIVE   (restore / unarchive)
 * </pre>
 */
public enum ProgramStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,
    ARCHIVED;

    public Set<ProgramStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> EnumSet.of(ACTIVE);
            case ACTIVE -> EnumSet.of(INACTIVE);
            case INACTIVE -> EnumSet.of(ACTIVE, ARCHIVED);
            // ARCHIVED is reversible: restore to INACTIVE (from which it can be re-activated).
            case ARCHIVED -> EnumSet.of(INACTIVE);
        };
    }

    public boolean canTransitionTo(ProgramStatus target) {
        return allowedNext().contains(target);
    }
}
