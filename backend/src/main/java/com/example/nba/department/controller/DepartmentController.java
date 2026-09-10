package com.example.nba.department.controller;

import com.example.nba.common.response.ApiResponse;
import com.example.nba.common.response.PageResponse;
import com.example.nba.department.dto.DepartmentRequest;
import com.example.nba.department.dto.DepartmentResponse;
import com.example.nba.department.dto.DepartmentStatusRequest;
import com.example.nba.department.entity.DepartmentStatus;
import com.example.nba.department.service.DepartmentService;
import com.example.nba.program.dto.ProgramResponse;
import com.example.nba.program.entity.ProgramStatus;
import com.example.nba.program.service.ProgramService;
import com.example.nba.security.PermissionCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Departments", description = "Department management")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;
    private final ProgramService programService;

    @Operation(summary = "List departments", description = "Requires DEPARTMENT_VIEW. Filter by institutionId/status, search, paginate.")
    @PreAuthorize(PermissionCatalog.HAS_DEPARTMENT_VIEW)
    @GetMapping
    public ApiResponse<PageResponse<DepartmentResponse>> list(
            @RequestParam(required = false) UUID institutionId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DepartmentStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(institutionId, search, status, pageable)));
    }

    @Operation(summary = "Get department by id", description = "Requires DEPARTMENT_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_DEPARTMENT_VIEW)
    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "List programs under a department", description = "Requires PROGRAM_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_VIEW)
    @GetMapping("/{id}/programs")
    public ApiResponse<PageResponse<ProgramResponse>> programs(
            @PathVariable UUID id,
            @RequestParam(required = false) ProgramStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(programService.list(id, status, search, pageable)));
    }

    @Operation(summary = "Create department", description = "Requires DEPARTMENT_CREATE. Code unique within institution.")
    @PreAuthorize(PermissionCatalog.HAS_DEPARTMENT_CREATE)
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Department created successfully"));
    }

    @Operation(summary = "Update department", description = "Requires DEPARTMENT_UPDATE. institutionId is immutable.")
    @PreAuthorize(PermissionCatalog.HAS_DEPARTMENT_UPDATE)
    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.ok(service.update(id, request), "Department updated successfully");
    }

    @Operation(summary = "Change department status", description = "Requires DEPARTMENT_STATUS_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_DEPARTMENT_STATUS_UPDATE)
    @PatchMapping("/{id}/status")
    public ApiResponse<DepartmentResponse> changeStatus(@PathVariable UUID id,
                                                        @Valid @RequestBody DepartmentStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()), "Department status updated");
    }
}
