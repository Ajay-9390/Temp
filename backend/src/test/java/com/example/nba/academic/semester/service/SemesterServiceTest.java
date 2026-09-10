package com.example.nba.academic.semester.service;

import com.example.nba.academic.AcademicLifecycleValidator;
import com.example.nba.academic.semester.dto.SemesterRequest;
import com.example.nba.academic.semester.entity.Semester;
import com.example.nba.academic.semester.mapper.SemesterMapper;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.BusinessValidationException;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.security.DataScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock SemesterRepository semesterRepository;
    @Mock AcademicYearRepository academicYearRepository;
    @Mock AuditService auditService;
    @Mock DataScopeService dataScope;

    SemesterService service;

    private final UUID yearId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SemesterMapper mapper = Mappers.getMapper(SemesterMapper.class);
        service = new SemesterService(semesterRepository, academicYearRepository, mapper,
                new AcademicLifecycleValidator(), auditService, dataScope);

        AcademicYear year = new AcademicYear();
        year.setProgramId(UUID.randomUUID());
        year.setStartDate(LocalDate.of(2026, 7, 1));
        year.setEndDate(LocalDate.of(2027, 6, 30));
        lenient().when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(year));
    }

    private SemesterRequest request(LocalDate start, LocalDate end) {
        return new SemesterRequest(1, "Semester 1", start, end);
    }

    @Test
    void createSucceedsWhenDatesWithinYear() {
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(yearId, 1)).thenReturn(false);
        when(semesterRepository.save(any(Semester.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(yearId,
                request(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31)));

        assertThat(response.semesterNumber()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Semester 1");
    }

    @Test
    void createFailsWhenSemesterStartsBeforeYear() {
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(yearId, 1)).thenReturn(false);

        assertThatThrownBy(() -> service.create(yearId,
                request(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31))))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("within the academic year");
    }

    @Test
    void createFailsWhenSemesterEndsAfterYear() {
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(yearId, 1)).thenReturn(false);

        assertThatThrownBy(() -> service.create(yearId,
                request(LocalDate.of(2026, 7, 1), LocalDate.of(2027, 8, 1))))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("within the academic year");
    }

    @Test
    void createFailsWhenSemesterNumberDuplicate() {
        when(semesterRepository.existsByAcademicYearIdAndSemesterNumber(yearId, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.create(yearId,
                request(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 31))))
                .isInstanceOf(ConflictException.class);
    }
}
