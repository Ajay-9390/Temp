package com.example.nba.accreditation.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Accreditation cycle lifecycle states and allowed transitions.
 * <pre>
 * DRAFT        → PREPARATION, CANCELLED
 * PREPARATION  → SUBMITTED, CANCELLED
 * SUBMITTED    → UNDER_REVIEW, CANCELLED
 * UNDER_REVIEW → ACCREDITED, REJECTED
 * ACCREDITED   → EXPIRED
 * REJECTED     → PREPARATION
 * EXPIRED / CANCELLED → (terminal)
 * </pre>
 */
public enum AccreditationCycleStatus {
    DRAFT,
    PREPARATION,
    SUBMITTED,
    UNDER_REVIEW,
    ACCREDITED,
    EXPIRED,
    REJECTED,
    CANCELLED;

    public Set<AccreditationCycleStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> EnumSet.of(PREPARATION, CANCELLED);
            case PREPARATION -> EnumSet.of(SUBMITTED, CANCELLED);
            case SUBMITTED -> EnumSet.of(UNDER_REVIEW, CANCELLED);
            case UNDER_REVIEW -> EnumSet.of(ACCREDITED, REJECTED);
            case ACCREDITED -> EnumSet.of(EXPIRED);
            case REJECTED -> EnumSet.of(PREPARATION);
            case EXPIRED, CANCELLED -> EnumSet.noneOf(AccreditationCycleStatus.class);
        };
    }

    public boolean canTransitionTo(AccreditationCycleStatus target) {
        return allowedNext().contains(target);
    }

    /** Statuses considered "active/occupying" a program for duplicate-active prevention. */
    public boolean isActiveLifecycle() {
        return this == DRAFT || this == PREPARATION || this == SUBMITTED || this == UNDER_REVIEW;
    }
}
