package com.example.nba.academic.semester.service;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.academic.AcademicLifecycleValidator;
import com.example.nba.academic.semester.dto.SemesterRequest;
import com.example.nba.academic.semester.dto.SemesterResponse;
import com.example.nba.academic.semester.entity.Semester;
import com.example.nba.academic.semester.mapper.SemesterMapper;
import com.example.nba.academic.semester.repository.SemesterRepository;
import com.example.nba.academic.year.entity.AcademicYear;
import com.example.nba.academic.year.repository.AcademicYearRepository;
import com.example.nba.common.audit.AuditAction;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.BusinessValidationException;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.exception.ResourceNotFoundException;
import com.example.nba.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository repository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterMapper mapper;
    private final AcademicLifecycleValidator lifecycleValidator;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public List<SemesterResponse> listByYear(UUID academicYearId) {
        getYear(academicYearId);
        return repository.findByAcademicYearIdOrderBySemesterNumberAsc(academicYearId).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse get(UUID id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Semester getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SEMESTER_NOT_FOUND, "Semester not found: " + id));
    }

    @Transactional
    public SemesterResponse create(UUID academicYearId, SemesterRequest request) {
        AcademicYear year = getYear(academicYearId);
        dataScope.assertProgramAccess(year.getProgramId());
        if (repository.existsByAcademicYearIdAndSemesterNumber(academicYearId, request.semesterNumber())) {
            throw new ConflictException(ErrorCode.SEMESTER_NUMBER_ALREADY_EXISTS,
                    "Semester number " + request.semesterNumber() + " already exists in this academic year");
        }
        validateDatesWithinYear(request.startDate(), request.endDate(), year);
        Semester entity = mapper.toEntity(request);
        entity.setAcademicYearId(academicYearId);
        entity.setStatus(AcademicLifecycleStatus.PLANNED);
        Semester saved = repository.save(entity);
        auditService.record("Semester", saved.getId(), AuditAction.CREATE, null, saved.getName());
        return mapper.toResponse(saved);
    }

    @Transactional
    public SemesterResponse update(UUID id, SemesterRequest request) {
        Semester entity = getEntity(id);
        AcademicYear year = getYear(entity.getAcademicYearId());
        dataScope.assertProgramAccess(year.getProgramId());
        if (repository.existsByAcademicYearIdAndSemesterNumberAndIdNot(
                entity.getAcademicYearId(), request.semesterNumber(), id)) {
            throw new ConflictException(ErrorCode.SEMESTER_NUMBER_ALREADY_EXISTS,
                    "Semester number " + request.semesterNumber() + " already exists in this academic year");
        }
        validateDatesWithinYear(request.startDate(), request.endDate(), year);
        mapper.updateEntity(request, entity);
        auditService.record("Semester", id, AuditAction.UPDATE);
        return mapper.toResponse(entity);
    }

    @Transactional
    public SemesterResponse changeStatus(UUID id, AcademicLifecycleStatus target) {
        Semester entity = getEntity(id);
        AcademicYear year = getYear(entity.getAcademicYearId());
        dataScope.assertProgramAccess(year.getProgramId());
        AcademicLifecycleStatus current = entity.getStatus();
        lifecycleValidator.validateTransition(current, target);
        entity.setStatus(target);
        AuditAction action = target == AcademicLifecycleStatus.ARCHIVED
                ? AuditAction.ARCHIVE : AuditAction.STATUS_CHANGE;
        auditService.record("Semester", id, action, current.name(), target.name());
        return mapper.toResponse(entity);
    }

    // ---------------------------------------------------------------------

    private void validateDatesWithinYear(LocalDate start, LocalDate end, AcademicYear year) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new BusinessValidationException(ErrorCode.INVALID_DATE_RANGE,
                    "startDate must be before endDate");
        }
        if (start != null && start.isBefore(year.getStartDate())) {
            throw new BusinessValidationException(ErrorCode.SEMESTER_DATES_OUT_OF_RANGE,
                    "Semester startDate must fall within the academic year (" + year.getStartDate()
                            + " to " + year.getEndDate() + ")");
        }
        if (end != null && end.isAfter(year.getEndDate())) {
            throw new BusinessValidationException(ErrorCode.SEMESTER_DATES_OUT_OF_RANGE,
                    "Semester endDate must fall within the academic year (" + year.getStartDate()
                            + " to " + year.getEndDate() + ")");
        }
    }

    private AcademicYear getYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId).orElseThrow(() ->
                new ResourceNotFoundException(ErrorCode.ACADEMIC_YEAR_NOT_FOUND,
                        "Academic year not found: " + academicYearId));
    }
}
