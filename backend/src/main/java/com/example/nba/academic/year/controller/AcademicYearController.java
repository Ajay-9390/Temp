package com.example.nba.academic.year.controller;

import com.example.nba.academic.year.dto.AcademicYearRequest;
import com.example.nba.academic.year.dto.AcademicYearResponse;
import com.example.nba.academic.year.dto.AcademicYearStatusRequest;
import com.example.nba.academic.year.service.AcademicYearService;
import com.example.nba.common.response.ApiResponse;
import com.example.nba.security.PermissionCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Academic Years", description = "Academic year management + lifecycle")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService service;

    @Operation(summary = "List a program's academic years", description = "Requires ACADEMIC_YEAR_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_YEAR_VIEW)
    @GetMapping("/programs/{programId}/academic-years")
    public ApiResponse<List<AcademicYearResponse>> listByProgram(@PathVariable UUID programId) {
        return ApiResponse.ok(service.listByProgram(programId));
    }

    @Operation(summary = "Create academic year",
            description = "Requires ACADEMIC_YEAR_CREATE. Validates date range and prevents duplicate year names per program.")
    @PreAuthorize(PermissionCatalog.HAS_YEAR_CREATE)
    @PostMapping("/programs/{programId}/academic-years")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> create(
            @PathVariable UUID programId, @Valid @RequestBody AcademicYearRequest request) {
        AcademicYearResponse created = service.create(programId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Academic year created successfully"));
    }

    @Operation(summary = "Get academic year by id", description = "Requires ACADEMIC_YEAR_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_YEAR_VIEW)
    @GetMapping("/academic-years/{id}")
    public ApiResponse<AcademicYearResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Update academic year", description = "Requires ACADEMIC_YEAR_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_YEAR_UPDATE)
    @PutMapping("/academic-years/{id}")
    public ApiResponse<AcademicYearResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody AcademicYearRequest request) {
        return ApiResponse.ok(service.update(id, request), "Academic year updated successfully");
    }

    @Operation(summary = "Change academic year status (activate/complete/archive)",
            description = "Requires ACADEMIC_YEAR_STATUS_UPDATE. Transitions: PLANNED→ACTIVE→COMPLETED→ARCHIVED.")
    @PreAuthorize(PermissionCatalog.HAS_YEAR_STATUS_UPDATE)
    @PatchMapping("/academic-years/{id}/status")
    public ApiResponse<AcademicYearResponse> changeStatus(
            @PathVariable UUID id, @Valid @RequestBody AcademicYearStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()), "Academic year status updated");
    }
}
