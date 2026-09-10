package com.accreditation.nba.evidence.workflow;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.exception.InvalidStateTransitionException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Custom evidence workflow (no Camunda). The single authority for valid status transitions;
 * business rules live here in the service layer, never in controllers.
 *
 * <pre>
 * DRAFT            -> SUBMITTED, ARCHIVED
 * SUBMITTED        -> UNDER_REVIEW, DRAFT, ARCHIVED
 * UNDER_REVIEW     -> APPROVED, REJECTED, CHANGES_REQUIRED
 * CHANGES_REQUIRED -> SUBMITTED, DRAFT, ARCHIVED
 * APPROVED         -> ARCHIVED, DRAFT (revision via new version)
 * REJECTED         -> ARCHIVED, DRAFT (revision via new version)
 * ARCHIVED         -> (terminal)
 * </pre>
 */
@Component
public class EvidenceWorkflow {

    private static final Map<EvidenceStatus, Set<EvidenceStatus>> ALLOWED = new EnumMap<>(EvidenceStatus.class);

    static {
        ALLOWED.put(EvidenceStatus.DRAFT, Set.of(EvidenceStatus.SUBMITTED, EvidenceStatus.ARCHIVED));
        ALLOWED.put(EvidenceStatus.SUBMITTED, Set.of(EvidenceStatus.UNDER_REVIEW, EvidenceStatus.DRAFT, EvidenceStatus.ARCHIVED));
        ALLOWED.put(EvidenceStatus.UNDER_REVIEW, Set.of(EvidenceStatus.APPROVED, EvidenceStatus.REJECTED, EvidenceStatus.CHANGES_REQUIRED));
        ALLOWED.put(EvidenceStatus.CHANGES_REQUIRED, Set.of(EvidenceStatus.SUBMITTED, EvidenceStatus.DRAFT, EvidenceStatus.ARCHIVED));
        ALLOWED.put(EvidenceStatus.APPROVED, Set.of(EvidenceStatus.ARCHIVED, EvidenceStatus.DRAFT));
        ALLOWED.put(EvidenceStatus.REJECTED, Set.of(EvidenceStatus.ARCHIVED, EvidenceStatus.DRAFT));
        ALLOWED.put(EvidenceStatus.ARCHIVED, Set.of());
    }

    public boolean canTransition(EvidenceStatus from, EvidenceStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    /** @throws InvalidStateTransitionException if the transition is not permitted. */
    public void assertCanTransition(EvidenceStatus from, EvidenceStatus to) {
        if (!canTransition(from, to)) {
            throw new InvalidStateTransitionException(from, to);
        }
    }

    public Set<EvidenceStatus> allowedTargets(EvidenceStatus from) {
        return ALLOWED.getOrDefault(from, Set.of());
    }
}
