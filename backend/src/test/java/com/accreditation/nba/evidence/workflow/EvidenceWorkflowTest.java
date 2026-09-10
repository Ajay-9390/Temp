package com.accreditation.nba.evidence.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvidenceWorkflowTest {

    private EvidenceWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new EvidenceWorkflow();
    }

    @Test
    void allowsValidLifecyclePath() {
        assertThat(workflow.canTransition(EvidenceStatus.DRAFT, EvidenceStatus.SUBMITTED)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.SUBMITTED, EvidenceStatus.UNDER_REVIEW)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.UNDER_REVIEW, EvidenceStatus.APPROVED)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.UNDER_REVIEW, EvidenceStatus.CHANGES_REQUIRED)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.CHANGES_REQUIRED, EvidenceStatus.SUBMITTED)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.UNDER_REVIEW, EvidenceStatus.REJECTED)).isTrue();
    }

    @Test
    void allowsRevisionFromDecidedStates() {
        assertThat(workflow.canTransition(EvidenceStatus.APPROVED, EvidenceStatus.DRAFT)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.REJECTED, EvidenceStatus.DRAFT)).isTrue();
        assertThat(workflow.canTransition(EvidenceStatus.APPROVED, EvidenceStatus.ARCHIVED)).isTrue();
    }

    @Test
    void rejectsInvalidTransitions() {
        assertThat(workflow.canTransition(EvidenceStatus.DRAFT, EvidenceStatus.APPROVED)).isFalse();
        assertThat(workflow.canTransition(EvidenceStatus.APPROVED, EvidenceStatus.UNDER_REVIEW)).isFalse();
        assertThat(workflow.canTransition(EvidenceStatus.SUBMITTED, EvidenceStatus.APPROVED)).isFalse();
    }

    @Test
    void archivedIsTerminal() {
        assertThat(workflow.allowedTargets(EvidenceStatus.ARCHIVED)).isEmpty();
        assertThat(workflow.canTransition(EvidenceStatus.ARCHIVED, EvidenceStatus.DRAFT)).isFalse();
    }

    @Test
    void sameStateIsNotATransition() {
        assertThat(workflow.canTransition(EvidenceStatus.DRAFT, EvidenceStatus.DRAFT)).isFalse();
    }

    @Test
    void assertCanTransitionThrowsForInvalid() {
        assertThatThrownBy(() ->
                workflow.assertCanTransition(EvidenceStatus.APPROVED, EvidenceStatus.UNDER_REVIEW))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
