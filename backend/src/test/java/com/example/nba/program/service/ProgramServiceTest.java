package com.example.nba.program.service;

import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.accreditation.repository.AccreditationCycleRepository;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.department.entity.Department;
import com.example.nba.department.repository.DepartmentRepository;
import com.example.nba.institution.repository.InstitutionRepository;
import com.example.nba.program.dto.ProgramRequest;
import com.example.nba.program.dto.ProgramResponse;
import com.example.nba.program.entity.Program;
import com.example.nba.program.entity.ProgramStatus;
import com.example.nba.program.lifecycle.ProgramLifecycleService;
import com.example.nba.program.mapper.ProgramMapper;
import com.example.nba.program.repository.ProgramRepository;
import com.example.nba.security.DataScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock ProgramRepository programRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock InstitutionRepository institutionRepository;
    @Mock AccreditationCycleRepository cycleRepository;
    @Mock AcademicYearRepository academicYearRepository;
    @Mock SemesterRepository semesterRepository;
    @Mock AuditService auditService;
    @Mock DataScopeService dataScope;

    ProgramService service;

    private final UUID deptId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ProgramMapper mapper = Mappers.getMapper(ProgramMapper.class);
        service = new ProgramService(programRepository, departmentRepository, institutionRepository,
                cycleRepository, academicYearRepository, semesterRepository, mapper,
                new ProgramLifecycleService(), auditService, dataScope);

        Department dept = new Department();
        dept.setName("CSE");
        lenient().when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        lenient().when(cycleRepository.findByProgramIdOrderByApplicationYearDesc(any()))
                .thenReturn(List.of());
        lenient().when(academicYearRepository.findFirstByProgramIdAndStatus(any(), any()))
                .thenReturn(Optional.empty());
    }

    private ProgramRequest request(String code) {
        return new ProgramRequest(deptId, "B.Tech CSE", code, "B.Tech",
                "Computer Science and Engineering", null, 4, 8, 120, 2010);
    }

    @Test
    void createSucceedsWhenCodeIsUnique() {
        when(programRepository.existsByDepartmentIdAndCodeIgnoreCase(deptId, "BTECH-CSE")).thenReturn(false);
        when(programRepository.save(any(Program.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgramResponse response = service.create(request("BTECH-CSE"));

        assertThat(response.code()).isEqualTo("BTECH-CSE");
        assertThat(response.status()).isEqualTo(ProgramStatus.DRAFT);
        assertThat(response.departmentName()).isEqualTo("CSE");
    }

    @Test
    void createFailsWhenCodeAlreadyExistsInDepartment() {
        when(programRepository.existsByDepartmentIdAndCodeIgnoreCase(deptId, "BTECH-CSE")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("BTECH-CSE")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void changeStatusRejectsInvalidTransition() {
        Program program = new Program();
        program.setDepartmentId(deptId);
        program.setStatus(ProgramStatus.DRAFT);
        UUID id = program.getId();
        when(programRepository.findById(id)).thenReturn(Optional.of(program));

        assertThatThrownBy(() -> service.changeStatus(id, ProgramStatus.ARCHIVED))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void changeStatusAppliesValidTransition() {
        Program program = new Program();
        program.setDepartmentId(deptId);
        program.setStatus(ProgramStatus.DRAFT);
        UUID id = program.getId();
        when(programRepository.findById(id)).thenReturn(Optional.of(program));

        ProgramResponse response = service.changeStatus(id, ProgramStatus.ACTIVE);

        assertThat(response.status()).isEqualTo(ProgramStatus.ACTIVE);
    }
}
