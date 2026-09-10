package com.example.nba.program.service;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.accreditation.entity.AccreditationCycle;
import com.example.nba.accreditation.repository.AccreditationCycleRepository;
import com.example.nba.common.audit.AuditAction;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.exception.ResourceNotFoundException;
import com.example.nba.department.entity.Department;
import com.example.nba.department.repository.DepartmentRepository;
import com.example.nba.institution.repository.InstitutionRepository;
import com.example.nba.program.dto.ProgramOverviewResponse;
import com.example.nba.program.dto.ProgramRequest;
import com.example.nba.program.dto.ProgramResponse;
import com.example.nba.program.entity.Program;
import com.example.nba.program.entity.ProgramStatus;
import com.example.nba.program.lifecycle.ProgramLifecycleService;
import com.example.nba.program.mapper.ProgramMapper;
import com.example.nba.program.repository.ProgramRepository;
import com.example.nba.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository repository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;
    private final AccreditationCycleRepository cycleRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final ProgramMapper mapper;
    private final ProgramLifecycleService lifecycleService;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public Page<ProgramResponse> list(UUID departmentId, ProgramStatus status, String search,
                                      Pageable pageable) {
        Specification<Program> spec = Specification.where(null);
        if (departmentId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("departmentId"), departmentId));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            // Also match programs whose DEPARTMENT name/code matches the search term.
            List<UUID> matchingDeptIds = departmentRepository.findIdsByNameOrCodeLike(search);
            spec = spec.and((root, q, cb) -> {
                var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                predicates.add(cb.like(cb.lower(root.get("name")), like));
                predicates.add(cb.like(cb.lower(root.get("code")), like));
                predicates.add(cb.like(cb.lower(root.get("degree")), like));
                predicates.add(cb.like(cb.lower(root.get("branch")), like));
                if (!matchingDeptIds.isEmpty()) {
                    predicates.add(root.get("departmentId").in(matchingDeptIds));
                }
                return cb.or(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
            });
        }
        return repository.findAll(spec, pageable).map(this::enrich);
    }

    @Transactional(readOnly = true)
    public ProgramResponse get(UUID id) {
        return enrich(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Program getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.PROGRAM_NOT_FOUND, "Program not found: " + id));
    }

    @Transactional
    public ProgramResponse create(ProgramRequest request) {
        Department dept = getDepartment(request.departmentId());
        dataScope.assertDepartmentAccess(dept.getId());
        if (repository.existsByDepartmentIdAndCodeIgnoreCase(request.departmentId(), request.code())) {
            throw new ConflictException(ErrorCode.PROGRAM_CODE_ALREADY_EXISTS,
                    "Program code already exists in this department: " + request.code());
        }
        Program entity = mapper.toEntity(request);
        entity.setStatus(ProgramStatus.DRAFT);
        Program saved = repository.save(entity);
        auditService.record("Program", saved.getId(), AuditAction.CREATE, null, saved.getCode());
        return enrich(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "programs", key = "#id")
    public ProgramResponse update(UUID id, ProgramRequest request) {
        Program entity = getEntity(id);
        dataScope.assertProgramAccess(id);
        if (repository.existsByDepartmentIdAndCodeIgnoreCaseAndIdNot(
                entity.getDepartmentId(), request.code(), id)) {
            throw new ConflictException(ErrorCode.PROGRAM_CODE_ALREADY_EXISTS,
                    "Program code already exists in this department: " + request.code());
        }
        mapper.updateEntity(request, entity);   // departmentId ignored (immutable)
        auditService.record("Program", id, AuditAction.UPDATE);
        return enrich(entity);
    }

    @Transactional
    @CacheEvict(cacheNames = "programs", key = "#id")
    public ProgramResponse changeStatus(UUID id, ProgramStatus target) {
        Program entity = getEntity(id);
        dataScope.assertProgramAccess(id);
        ProgramStatus current = entity.getStatus();
        lifecycleService.validateTransition(current, target);
        entity.setStatus(target);
        AuditAction action = target == ProgramStatus.ARCHIVED ? AuditAction.ARCHIVE : AuditAction.STATUS_CHANGE;
        auditService.record("Program", id, action, current.name(), target.name());
        return enrich(entity);
    }

    @Transactional(readOnly = true)
    public ProgramOverviewResponse getOverview(UUID id) {
        Program program = getEntity(id);
        Department dept = getDepartment(program.getDepartmentId());
        String institutionName = institutionRepository.findById(dept.getInstitutionId())
                .map(i -> i.getName()).orElse(null);

        List<AccreditationCycle> cycles = cycleRepository
                .findByProgramIdOrderByApplicationYearDesc(id);
        AccreditationCycle current = pickCurrentCycle(cycles);
        List<ProgramResponse.CurrentAccreditation> previous = cycles.stream()
                .filter(c -> current == null || !c.getId().equals(current.getId()))
                .map(this::toAccreditationSummary)
                .toList();

        List<AcademicYear> years = academicYearRepository.findByProgramIdOrderByStartDateDesc(id);
        AcademicYear activeYear = years.stream()
                .filter(y -> y.getStatus() == AcademicLifecycleStatus.ACTIVE)
                .findFirst().orElse(null);

        return new ProgramOverviewResponse(
                program.getId(),
                program.getName(),
                program.getCode(),
                program.getStatus(),
                dept.getId(),
                dept.getName(),
                dept.getInstitutionId(),
                institutionName,
                current == null ? null : toAccreditationSummary(current),
                current == null ? null : current.getTier(),
                activeYear == null ? null
                        : new ProgramResponse.CurrentAcademicYear(activeYear.getId(),
                        activeYear.getName(), activeYear.getStatus().name()),
                program.getTotalSemesters(),
                cycles.size(),
                years.size(),
                previous,
                ProgramOverviewResponse.defaultIntegrationPoints());
    }

    // ---------------------------------------------------------------------

    private Department getDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId).orElseThrow(() ->
                new ResourceNotFoundException(ErrorCode.DEPARTMENT_NOT_FOUND,
                        "Department not found: " + departmentId));
    }

    /** Prefer an in-progress cycle; otherwise the most recent by application year. */
    private AccreditationCycle pickCurrentCycle(List<AccreditationCycle> cycles) {
        return cycles.stream()
                .filter(c -> c.getStatus().isActiveLifecycle())
                .max(Comparator.comparing(AccreditationCycle::getApplicationYear))
                .orElse(cycles.isEmpty() ? null : cycles.get(0));
    }

    private ProgramResponse.CurrentAccreditation toAccreditationSummary(AccreditationCycle c) {
        return new ProgramResponse.CurrentAccreditation(
                c.getId(), c.getName(), c.getTier(), c.getStatus().name());
    }

    /** Builds a fully enriched program response (department name + current cycle + active year). */
    private ProgramResponse enrich(Program program) {
        ProgramResponse base = mapper.toResponse(program);
        String departmentName = departmentRepository.findById(program.getDepartmentId())
                .map(Department::getName).orElse(null);

        List<AccreditationCycle> cycles = cycleRepository
                .findByProgramIdOrderByApplicationYearDesc(program.getId());
        AccreditationCycle current = pickCurrentCycle(cycles);

        AcademicYear activeYear = academicYearRepository
                .findFirstByProgramIdAndStatus(program.getId(), AcademicLifecycleStatus.ACTIVE)
                .orElse(null);

        return new ProgramResponse(
                base.id(), base.departmentId(), departmentName, base.name(), base.code(),
                base.degree(), base.branch(), base.description(), base.durationYears(),
                base.totalSemesters(), base.intake(), base.establishedYear(), base.status(),
                current == null ? null : toAccreditationSummary(current),
                activeYear == null ? null
                        : new ProgramResponse.CurrentAcademicYear(activeYear.getId(),
                        activeYear.getName(), activeYear.getStatus().name()),
                base.createdAt(), base.updatedAt(), base.createdBy(), base.updatedBy());
    }
}
