package com.example.nba.accreditation.lifecycle;

import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import com.example.nba.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccreditationCycleLifecycleServiceTest {

    private final AccreditationCycleLifecycleService service = new AccreditationCycleLifecycleService();

    @ParameterizedTest
    @CsvSource({
            "DRAFT,PREPARATION",
            "PREPARATION,SUBMITTED",
            "SUBMITTED,UNDER_REVIEW",
            "UNDER_REVIEW,ACCREDITED",
            "UNDER_REVIEW,REJECTED",
            "ACCREDITED,EXPIRED",
            "REJECTED,PREPARATION",
            "DRAFT,CANCELLED"
    })
    void allowsValidTransitions(AccreditationCycleStatus from, AccreditationCycleStatus to) {
        assertThatCode(() -> service.validateTransition(from, to)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT,ACCREDITED",
            "PREPARATION,ACCREDITED",
            "ACCREDITED,PREPARATION",
            "EXPIRED,PREPARATION",
            "CANCELLED,DRAFT"
    })
    void rejectsInvalidTransitions(AccreditationCycleStatus from, AccreditationCycleStatus to) {
        assertThatThrownBy(() -> service.validateTransition(from, to))
                .isInstanceOf(ConflictException.class);
    }
}
