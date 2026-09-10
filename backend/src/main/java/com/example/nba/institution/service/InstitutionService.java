package com.example.nba.institution.service;

import com.example.nba.common.audit.AuditAction;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.exception.ResourceNotFoundException;
import com.example.nba.institution.dto.InstitutionRequest;
import com.example.nba.institution.dto.InstitutionResponse;
import com.example.nba.institution.entity.Institution;
import com.example.nba.institution.entity.InstitutionStatus;
import com.example.nba.institution.mapper.InstitutionMapper;
import com.example.nba.institution.repository.InstitutionRepository;
import com.example.nba.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Institution management. Basic reference management only; if the platform later provides a
 * shared Institution service, this module can consume that instead (see ARCHITECTURE.md §12).
 */
@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository repository;
    private final InstitutionMapper mapper;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public Page<InstitutionResponse> list(String search, InstitutionStatus status, Pageable pageable) {
        Specification<Institution> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("code")), like)));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InstitutionResponse get(UUID id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Institution getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.INSTITUTION_NOT_FOUND, "Institution not found: " + id));
    }

    @Transactional
    public InstitutionResponse create(InstitutionRequest request) {
        if (repository.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException(ErrorCode.INSTITUTION_CODE_ALREADY_EXISTS,
                    "Institution code already exists: " + request.code());
        }
        Institution entity = mapper.toEntity(request);
        entity.setStatus(InstitutionStatus.ACTIVE);
        Institution saved = repository.save(entity);
        auditService.record("Institution", saved.getId(), AuditAction.CREATE, null, saved.getCode());
        return mapper.toResponse(saved);
    }

    @Transactional
    public InstitutionResponse update(UUID id, InstitutionRequest request) {
        Institution entity = getEntity(id);
        dataScope.assertInstitutionAccess(id);
        if (repository.existsByCodeIgnoreCaseAndIdNot(request.code(), id)) {
            throw new ConflictException(ErrorCode.INSTITUTION_CODE_ALREADY_EXISTS,
                    "Institution code already exists: " + request.code());
        }
        mapper.updateEntity(request, entity);
        auditService.record("Institution", id, AuditAction.UPDATE);
        return mapper.toResponse(entity);
    }

    @Transactional
    public InstitutionResponse changeStatus(UUID id, InstitutionStatus status) {
        Institution entity = getEntity(id);
        dataScope.assertInstitutionAccess(id);
        InstitutionStatus old = entity.getStatus();
        entity.setStatus(status);
        auditService.record("Institution", id, AuditAction.STATUS_CHANGE, old.name(), status.name());
        return mapper.toResponse(entity);
    }
}
