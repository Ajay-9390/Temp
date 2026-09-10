package com.example.nba.program.lifecycle;

import com.example.nba.common.exception.ConflictException;
import com.example.nba.program.entity.ProgramStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProgramLifecycleServiceTest {

    private final ProgramLifecycleService service = new ProgramLifecycleService();

    @ParameterizedTest
    @CsvSource({
            "DRAFT,ACTIVE",
            "ACTIVE,INACTIVE",
            "INACTIVE,ACTIVE",
            "INACTIVE,ARCHIVED",
            "ARCHIVED,INACTIVE"
    })
    void allowsValidTransitions(ProgramStatus from, ProgramStatus to) {
        assertThatCode(() -> service.validateTransition(from, to)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT,ARCHIVED",
            "DRAFT,INACTIVE",
            "ACTIVE,ARCHIVED",
            "ACTIVE,DRAFT",
            "ARCHIVED,ACTIVE"
    })
    void rejectsInvalidTransitions(ProgramStatus from, ProgramStatus to) {
        assertThatThrownBy(() -> service.validateTransition(from, to))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsNoOpTransition() {
        assertThatThrownBy(() -> service.validateTransition(ProgramStatus.ACTIVE, ProgramStatus.ACTIVE))
                .isInstanceOf(ConflictException.class);
    }
}
