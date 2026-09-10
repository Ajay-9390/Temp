package com.example.nba.academic.year.service;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.AcademicLifecycleValidator;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.dto.AcademicYearRequest;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.mapper.AcademicYearMapper;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.program.repository.ProgramRepository;
import com.example.nba.security.DataScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

    @Mock AcademicYearRepository repository;
    @Mock SemesterRepository semesterRepository;
    @Mock ProgramRepository programRepository;
    @Mock AuditService auditService;
    @Mock DataScopeService dataScope;

    AcademicYearService service;

    private final UUID programId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AcademicYearMapper mapper = Mappers.getMapper(AcademicYearMapper.class);
        service = new AcademicYearService(repository, semesterRepository, programRepository, mapper,
                new AcademicLifecycleValidator(), auditService, dataScope);
        lenient().when(semesterRepository.countByAcademicYearId(any())).thenReturn(0L);
    }

    private AcademicYear year(String name, LocalDate start, LocalDate end, AcademicLifecycleStatus status) {
        AcademicYear y = new AcademicYear();
        y.setProgramId(programId);
        y.setName(name);
        y.setStartDate(start);
        y.setEndDate(end);
        y.setStatus(status);
        return y;
    }

    @Test
    void createRejectsOverlappingDateRange() {
        when(programRepository.existsById(programId)).thenReturn(true);
        when(repository.existsByProgramIdAndNameIgnoreCase(programId, "2026-27B")).thenReturn(false);
        when(repository.findByProgramId(programId)).thenReturn(List.of(
                year("2026-27", LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30),
                        AcademicLifecycleStatus.PLANNED)));

        AcademicYearRequest req = new AcademicYearRequest("2026-27B",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31)); // overlaps

        assertThatThrownBy(() -> service.create(programId, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("overlaps");
    }

    @Test
    void activatingYearAutoCompletesPreviouslyActiveYear() {
        AcademicYear target = year("2026-27", LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30),
                AcademicLifecycleStatus.PLANNED);
        AcademicYear previouslyActive = year("2025-26", LocalDate.of(2025, 7, 1), LocalDate.of(2026, 6, 30),
                AcademicLifecycleStatus.ACTIVE);
        UUID id = target.getId();

        when(repository.findById(id)).thenReturn(Optional.of(target));
        when(repository.findByProgramIdAndStatusAndIdNot(programId, AcademicLifecycleStatus.ACTIVE, id))
                .thenReturn(List.of(previouslyActive));

        var response = service.changeStatus(id, AcademicLifecycleStatus.ACTIVE);

        assertThat(response.status()).isEqualTo(AcademicLifecycleStatus.ACTIVE);
        assertThat(previouslyActive.getStatus()).isEqualTo(AcademicLifecycleStatus.COMPLETED);
    }

    @Test
    void archivedYearCanBeRestoredToCompleted() {
        AcademicYear archived = year("2024-25", LocalDate.of(2024, 7, 1), LocalDate.of(2025, 6, 30),
                AcademicLifecycleStatus.ARCHIVED);
        UUID id = archived.getId();
        when(repository.findById(id)).thenReturn(Optional.of(archived));

        var response = service.changeStatus(id, AcademicLifecycleStatus.COMPLETED);

        assertThat(response.status()).isEqualTo(AcademicLifecycleStatus.COMPLETED);
    }
}
