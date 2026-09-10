package com.example.nba.department.service;

import com.example.nba.common.audit.AuditAction;
import com.example.nba.common.audit.AuditService;
import com.example.nba.common.exception.ConflictException;
import com.example.nba.common.exception.ErrorCode;
import com.example.nba.common.exception.ResourceNotFoundException;
import com.example.nba.department.dto.DepartmentRequest;
import com.example.nba.department.dto.DepartmentResponse;
import com.example.nba.department.entity.Department;
import com.example.nba.department.entity.DepartmentStatus;
import com.example.nba.department.mapper.DepartmentMapper;
import com.example.nba.department.repository.DepartmentRepository;
import com.example.nba.institution.repository.InstitutionRepository;
import com.example.nba.program.repository.ProgramRepository;
import com.example.nba.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository repository;
    private final InstitutionRepository institutionRepository;
    private final ProgramRepository programRepository;
    private final DepartmentMapper mapper;
    private final AuditService auditService;
    private final DataScopeService dataScope;

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> list(UUID institutionId, String search, DepartmentStatus status,
                                         Pageable pageable) {
        Specification<Department> spec = Specification.where(null);
        if (institutionId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("institutionId"), institutionId));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("code")), like)));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Department getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.DEPARTMENT_NOT_FOUND, "Department not found: " + id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        verifyInstitutionExists(request.institutionId());
        dataScope.assertInstitutionAccess(request.institutionId());
        if (repository.existsByInstitutionIdAndCodeIgnoreCase(request.institutionId(), request.code())) {
            throw new ConflictException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS,
                    "Department code already exists in this institution: " + request.code());
        }
        Department entity = mapper.toEntity(request);
        entity.setStatus(DepartmentStatus.ACTIVE);
        Department saved = repository.save(entity);
        auditService.record("Department", saved.getId(), AuditAction.CREATE, null, saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department entity = getEntity(id);
        dataScope.assertDepartmentAccess(id);
        // code is unique within the (immutable) institution of this department
        if (repository.existsByInstitutionIdAndCodeIgnoreCaseAndIdNot(
                entity.getInstitutionId(), request.code(), id)) {
            throw new ConflictException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS,
                    "Department code already exists in this institution: " + request.code());
        }
        mapper.updateEntity(request, entity);   // institutionId ignored (immutable)
        auditService.record("Department", id, AuditAction.UPDATE);
        return toResponse(entity);
    }

    @Transactional
    public DepartmentResponse changeStatus(UUID id, DepartmentStatus status) {
        Department entity = getEntity(id);
        dataScope.assertDepartmentAccess(id);
        DepartmentStatus old = entity.getStatus();
        entity.setStatus(status);
        auditService.record("Department", id, AuditAction.STATUS_CHANGE, old.name(), status.name());
        return toResponse(entity);
    }

    private void verifyInstitutionExists(UUID institutionId) {
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException(ErrorCode.INSTITUTION_NOT_FOUND,
                    "Institution not found: " + institutionId);
        }
    }

    private DepartmentResponse toResponse(Department entity) {
        long programCount = programRepository.countByDepartmentId(entity.getId());
        return mapper.toResponse(entity, programCount);
    }
}
