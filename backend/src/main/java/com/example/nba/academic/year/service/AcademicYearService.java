package com.example.nba.academic.year.service;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.AcademicLifecycleValidator;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.dto.AcademicYearRequest;
import com.example.nba.academic.year.dto.AcademicYearResponse;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.mapper.AcademicYearMapper;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.common.audit.AuditAction;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.BusinessValidationException;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.exception.ResourceNotFoundException;
import com.example.nba.program.repository.ProgramRepository;
import com.example.nba.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository repository;
    private final SemesterRepository semesterRepository;
    private final ProgramRepository programRepository;
    private final AcademicYearMapper mapper;
    private final AcademicLifecycleValidator lifecycleValidator;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> listByProgram(UUID programId) {
        verifyProgramExists(programId);
        return repository.findByProgramIdOrderByStartDateDesc(programId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse get(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public AcademicYear getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.ACADEMIC_YEAR_NOT_FOUND, "Academic year not found: " + id));
    }

    @Transactional
    public AcademicYearResponse create(UUID programId, AcademicYearRequest request) {
        verifyProgramExists(programId);
        dataScope.assertProgramAccess(programId);
        validateDateRange(request);
        if (repository.existsByProgramIdAndNameIgnoreCase(programId, request.name())) {
            throw new ConflictException(ErrorCode.ACADEMIC_YEAR_ALREADY_EXISTS,
                    "Academic year '" + request.name() + "' already exists for this program");
        }
        assertNoDateOverlap(programId, request.startDate(), request.endDate(), null);
        AcademicYear entity = mapper.toEntity(request);
        entity.setProgramId(programId);
        entity.setStatus(AcademicLifecycleStatus.PLANNED);
        AcademicYear saved = repository.save(entity);
        auditService.record("AcademicYear", saved.getId(), AuditAction.CREATE, null, saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public AcademicYearResponse update(UUID id, AcademicYearRequest request) {
        AcademicYear entity = getEntity(id);
        dataScope.assertProgramAccess(entity.getProgramId());
        validateDateRange(request);
        if (repository.existsByProgramIdAndNameIgnoreCaseAndIdNot(
                entity.getProgramId(), request.name(), id)) {
            throw new ConflictException(ErrorCode.ACADEMIC_YEAR_ALREADY_EXISTS,
                    "Academic year '" + request.name() + "' already exists for this program");
        }
        assertNoDateOverlap(entity.getProgramId(), request.startDate(), request.endDate(), id);
        mapper.updateEntity(request, entity);
        auditService.record("AcademicYear", id, AuditAction.UPDATE);
        return toResponse(entity);
    }

    @Transactional
    public AcademicYearResponse changeStatus(UUID id, AcademicLifecycleStatus target) {
        AcademicYear entity = getEntity(id);
        dataScope.assertProgramAccess(entity.getProgramId());
        AcademicLifecycleStatus current = entity.getStatus();
        lifecycleValidator.validateTransition(current, target);

        // Enforce a single ACTIVE academic year per program: activating this one
        // auto-completes any other currently-active year.
        if (target == AcademicLifecycleStatus.ACTIVE) {
            for (AcademicYear other : repository.findByProgramIdAndStatusAndIdNot(
                    entity.getProgramId(), AcademicLifecycleStatus.ACTIVE, id)) {
                other.setStatus(AcademicLifecycleStatus.COMPLETED);
                auditService.record("AcademicYear", other.getId(), AuditAction.STATUS_CHANGE,
                        AcademicLifecycleStatus.ACTIVE.name(), AcademicLifecycleStatus.COMPLETED.name());
            }
        }

        entity.setStatus(target);
        AuditAction action = target == AcademicLifecycleStatus.ARCHIVED
                ? AuditAction.ARCHIVE : AuditAction.STATUS_CHANGE;
        auditService.record("AcademicYear", id, action, current.name(), target.name());
        return toResponse(entity);
    }

    // ---------------------------------------------------------------------

    private void validateDateRange(AcademicYearRequest request) {
        if (!request.startDate().isBefore(request.endDate())) {
            throw new BusinessValidationException(ErrorCode.INVALID_DATE_RANGE,
                    "startDate must be before endDate");
        }
    }

    /** Rejects an academic year whose date range overlaps another year of the same program. */
    private void assertNoDateOverlap(UUID programId, LocalDate start, LocalDate end, UUID excludeId) {
        for (AcademicYear other : repository.findByProgramId(programId)) {
            if (excludeId != null && other.getId().equals(excludeId)) {
                continue;
            }
            // inclusive overlap: start <= other.end && other.start <= end
            if (!start.isAfter(other.getEndDate()) && !other.getStartDate().isAfter(end)) {
                throw new ConflictException(ErrorCode.DATE_RANGE_OVERLAP,
                        "Date range overlaps existing academic year '" + other.getName() + "' ("
                                + other.getStartDate() + " to " + other.getEndDate() + ")");
            }
        }
    }

    private void verifyProgramExists(UUID programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException(ErrorCode.PROGRAM_NOT_FOUND,
                    "Program not found: " + programId);
        }
    }

    private AcademicYearResponse toResponse(AcademicYear entity) {
        long semesterCount = semesterRepository.countByAcademicYearId(entity.getId());
        return mapper.toResponse(entity, semesterCount);
    }
}
