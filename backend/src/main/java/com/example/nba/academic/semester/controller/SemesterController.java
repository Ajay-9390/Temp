package com.example.nba.academic.semester.controller;

import com.example.nba.academic.semester.dto.SemesterRequest;
import com.example.nba.academic.semester.dto.SemesterResponse;
import com.example.nba.academic.semester.dto.SemesterStatusRequest;
import com.example.nba.academic.semester.service.SemesterService;
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

@Tag(name = "Semesters", description = "Semester management + lifecycle")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService service;

    @Operation(summary = "List semesters in an academic year", description = "Requires SEMESTER_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_SEMESTER_VIEW)
    @GetMapping("/academic-years/{academicYearId}/semesters")
    public ApiResponse<List<SemesterResponse>> listByYear(@PathVariable UUID academicYearId) {
        return ApiResponse.ok(service.listByYear(academicYearId));
    }

    @Operation(summary = "Create semester",
            description = "Requires SEMESTER_CREATE. Semester number unique within the year; dates must fall within the academic year.")
    @PreAuthorize(PermissionCatalog.HAS_SEMESTER_CREATE)
    @PostMapping("/academic-years/{academicYearId}/semesters")
    public ResponseEntity<ApiResponse<SemesterResponse>> create(
            @PathVariable UUID academicYearId, @Valid @RequestBody SemesterRequest request) {
        SemesterResponse created = service.create(academicYearId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Semester created successfully"));
    }

    @Operation(summary = "Get semester by id", description = "Requires SEMESTER_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_SEMESTER_VIEW)
    @GetMapping("/semesters/{id}")
    public ApiResponse<SemesterResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Update semester", description = "Requires SEMESTER_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_SEMESTER_UPDATE)
    @PutMapping("/semesters/{id}")
    public ApiResponse<SemesterResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody SemesterRequest request) {
        return ApiResponse.ok(service.update(id, request), "Semester updated successfully");
    }

    @Operation(summary = "Change semester status (activate/complete/archive)",
            description = "Requires SEMESTER_STATUS_UPDATE. Transitions: PLANNED→ACTIVE→COMPLETED→ARCHIVED.")
    @PreAuthorize(PermissionCatalog.HAS_SEMESTER_STATUS_UPDATE)
    @PatchMapping("/semesters/{id}/status")
    public ApiResponse<SemesterResponse> changeStatus(
            @PathVariable UUID id, @Valid @RequestBody SemesterStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()), "Semester status updated");
    }
}
