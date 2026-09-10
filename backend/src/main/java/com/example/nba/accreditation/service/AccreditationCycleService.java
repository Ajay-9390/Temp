package com.example.nba.accreditation.service;

import com.example.nba.accreditation.AccreditationProperties;
import com.example.nba.accreditation.dto.AccreditationCycleRequest;
import com.example.nba.accreditation.dto.AccreditationCycleResponse;
import com.example.nba.accreditation.entity.AccreditationCycle;
import com.example.nba.accreditation.entity.AccreditationCycleStatus;
import com.example.nba.accreditation.lifecycle.AccreditationCycleLifecycleService;
import com.example.nba.accreditation.mapper.AccreditationCycleMapper;
import com.example.nba.accreditation.repository.AccreditationCycleRepository;
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
public class AccreditationCycleService {

    private static final List<AccreditationCycleStatus> ACTIVE_LIFECYCLE = List.of(
            AccreditationCycleStatus.DRAFT, AccreditationCycleStatus.PREPARATION,
            AccreditationCycleStatus.SUBMITTED, AccreditationCycleStatus.UNDER_REVIEW);

    private final AccreditationCycleRepository repository;
    private final ProgramRepository programRepository;
    private final AccreditationCycleMapper mapper;
    private final AccreditationCycleLifecycleService lifecycleService;
    private final AccreditationProperties properties;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public List<AccreditationCycleResponse> listByProgram(UUID programId) {
        verifyProgramExists(programId);
        return repository.findByProgramIdOrderByApplicationYearDesc(programId).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccreditationCycleResponse get(UUID id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public AccreditationCycle getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.ACCREDITATION_CYCLE_NOT_FOUND, "Accreditation cycle not found: " + id));
    }

    @Transactional
    public AccreditationCycleResponse create(UUID programId, AccreditationCycleRequest request) {
        verifyProgramExists(programId);
        dataScope.assertProgramAccess(programId);
        validateTier(request.tier());
        validateDateRange(request);

        if (!properties.allowMultipleActiveCycles()
                && repository.existsByProgramIdAndStatusIn(programId, ACTIVE_LIFECYCLE)) {
            throw new ConflictException(ErrorCode.ACTIVE_ACCREDITATION_CYCLE_EXISTS,
                    "An active accreditation cycle already exists for this program");
        }
        assertNoDateOverlap(programId, request.startDate(), request.endDate(), null);

        AccreditationCycle entity = mapper.toEntity(request);
        entity.setProgramId(programId);
        entity.setStatus(AccreditationCycleStatus.DRAFT);
        AccreditationCycle saved = repository.save(entity);
        auditService.record("AccreditationCycle", saved.getId(), AuditAction.CREATE, null, saved.getName());
        return mapper.toResponse(saved);
    }

    @Transactional
    public AccreditationCycleResponse update(UUID id, AccreditationCycleRequest request) {
        AccreditationCycle entity = getEntity(id);
        dataScope.assertProgramAccess(entity.getProgramId());
        validateTier(request.tier());
        validateDateRange(request);
        assertNoDateOverlap(entity.getProgramId(), request.startDate(), request.endDate(), id);
        mapper.updateEntity(request, entity);
        auditService.record("AccreditationCycle", id, AuditAction.UPDATE);
        return mapper.toResponse(entity);
    }

    @Transactional
    public AccreditationCycleResponse changeStatus(UUID id, AccreditationCycleStatus target, String remarks) {
        AccreditationCycle entity = getEntity(id);
        dataScope.assertProgramAccess(entity.getProgramId());
        AccreditationCycleStatus current = entity.getStatus();
        lifecycleService.validateTransition(current, target);
        entity.setStatus(target);
        if (remarks != null && !remarks.isBlank()) {
            entity.setRemarks(remarks);
        }
        auditService.record("AccreditationCycle", id, AuditAction.STATUS_CHANGE, current.name(), target.name());
        return mapper.toResponse(entity);
    }

    // ---------------------------------------------------------------------

    private void validateTier(String tier) {
        if (!properties.allowedTiers().contains(tier)) {
            throw new BusinessValidationException(ErrorCode.VALIDATION_FAILED,
                    "Unsupported tier '" + tier + "'. Allowed: " + properties.allowedTiers());
        }
    }

    private void validateDateRange(AccreditationCycleRequest request) {
        if (request.startDate() != null && request.endDate() != null
                && !request.startDate().isBefore(request.endDate())) {
            throw new BusinessValidationException(ErrorCode.INVALID_DATE_RANGE,
                    "startDate must be before endDate");
        }
    }

    /**
     * Rejects a cycle whose date range overlaps another cycle of the same program.
     * Only applies when both cycles have start and end dates (dates are optional here).
     */
    private void assertNoDateOverlap(UUID programId, LocalDate start, LocalDate end, UUID excludeId) {
        if (start == null || end == null) {
            return;
        }
        for (AccreditationCycle other : repository.findByProgramIdOrderByApplicationYearDesc(programId)) {
            if (excludeId != null && other.getId().equals(excludeId)) {
                continue;
            }
            if (other.getStartDate() == null || other.getEndDate() == null) {
                continue;
            }
            if (!start.isAfter(other.getEndDate()) && !other.getStartDate().isAfter(end)) {
                throw new ConflictException(ErrorCode.DATE_RANGE_OVERLAP,
                        "Date range overlaps existing accreditation cycle '" + other.getName() + "' ("
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
}
