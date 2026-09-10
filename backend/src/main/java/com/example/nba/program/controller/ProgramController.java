package com.example.nba.program.controller;

import com.example.nba.common.response.ApiResponse;
import com.example.nba.common.response.PageResponse;
import com.example.nba.program.dto.ProgramOverviewResponse;
import com.example.nba.program.dto.ProgramRequest;
import com.example.nba.program.dto.ProgramResponse;
import com.example.nba.program.dto.ProgramStatusRequest;
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

@Tag(name = "Programs", description = "Program management + lifecycle")
@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService service;

    @Operation(summary = "List programs",
            description = "Requires PROGRAM_VIEW. Filter by departmentId/status, search across name/code/degree/branch, paginate, sort.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_VIEW)
    @GetMapping
    public ApiResponse<PageResponse<ProgramResponse>> list(
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) ProgramStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(departmentId, status, search, pageable)));
    }

    @Operation(summary = "Get program by id",
            description = "Requires PROGRAM_VIEW. Stable contract for future modules (Courses, SAR, OBE).")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_VIEW)
    @GetMapping("/{id}")
    public ApiResponse<ProgramResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Program overview/dashboard",
            description = "Requires PROGRAM_VIEW. Lightweight overview with placeholder integration points for future modules. No attainment calculations.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_VIEW)
    @GetMapping("/{id}/overview")
    public ApiResponse<ProgramOverviewResponse> overview(@PathVariable UUID id) {
        return ApiResponse.ok(service.getOverview(id));
    }

    @Operation(summary = "Create program", description = "Requires PROGRAM_CREATE. Code unique within department. Starts in DRAFT.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_CREATE)
    @PostMapping
    public ResponseEntity<ApiResponse<ProgramResponse>> create(@Valid @RequestBody ProgramRequest request) {
        ProgramResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Program created successfully"));
    }

    @Operation(summary = "Update program", description = "Requires PROGRAM_UPDATE. departmentId is immutable.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_UPDATE)
    @PutMapping("/{id}")
    public ApiResponse<ProgramResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody ProgramRequest request) {
        return ApiResponse.ok(service.update(id, request), "Program updated successfully");
    }

    @Operation(summary = "Change program status (lifecycle)",
            description = "Requires PROGRAM_STATUS_UPDATE. Enforces transitions: DRAFT→ACTIVE, ACTIVE→INACTIVE, INACTIVE→ACTIVE/ARCHIVED.")
    @PreAuthorize(PermissionCatalog.HAS_PROGRAM_STATUS_UPDATE)
    @PatchMapping("/{id}/status")
    public ApiResponse<ProgramResponse> changeStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody ProgramStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()), "Program status updated");
    }
}
