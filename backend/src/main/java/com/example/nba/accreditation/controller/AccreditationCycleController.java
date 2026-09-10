package com.example.nba.accreditation.controller;

import com.example.nba.accreditation.dto.AccreditationCycleRequest;
import com.example.nba.accreditation.dto.AccreditationCycleResponse;
import com.example.nba.accreditation.dto.AccreditationCycleStatusRequest;
import com.example.nba.accreditation.service.AccreditationCycleService;
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

@Tag(name = "Accreditation Cycles", description = "Accreditation cycle management + lifecycle")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccreditationCycleController {

    private final AccreditationCycleService service;

    @Operation(summary = "List a program's accreditation cycles",
            description = "Requires ACCREDITATION_CYCLE_VIEW. Ordered by application year (newest first).")
    @PreAuthorize(PermissionCatalog.HAS_CYCLE_VIEW)
    @GetMapping("/programs/{programId}/accreditation-cycles")
    public ApiResponse<List<AccreditationCycleResponse>> listByProgram(@PathVariable UUID programId) {
        return ApiResponse.ok(service.listByProgram(programId));
    }

    @Operation(summary = "Create accreditation cycle",
            description = "Requires ACCREDITATION_CYCLE_CREATE. Validates tier, date range, and duplicate active cycles.")
    @PreAuthorize(PermissionCatalog.HAS_CYCLE_CREATE)
    @PostMapping("/programs/{programId}/accreditation-cycles")
    public ResponseEntity<ApiResponse<AccreditationCycleResponse>> create(
            @PathVariable UUID programId, @Valid @RequestBody AccreditationCycleRequest request) {
        AccreditationCycleResponse created = service.create(programId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Accreditation cycle created successfully"));
    }

    @Operation(summary = "Get accreditation cycle by id", description = "Requires ACCREDITATION_CYCLE_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_CYCLE_VIEW)
    @GetMapping("/accreditation-cycles/{id}")
    public ApiResponse<AccreditationCycleResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Update accreditation cycle", description = "Requires ACCREDITATION_CYCLE_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_CYCLE_UPDATE)
    @PutMapping("/accreditation-cycles/{id}")
    public ApiResponse<AccreditationCycleResponse> update(@PathVariable UUID id,
                                                          @Valid @RequestBody AccreditationCycleRequest request) {
        return ApiResponse.ok(service.update(id, request), "Accreditation cycle updated successfully");
    }

    @Operation(summary = "Change accreditation cycle status (lifecycle)",
            description = "Requires ACCREDITATION_CYCLE_STATUS_UPDATE. Enforces the accreditation state machine.")
    @PreAuthorize(PermissionCatalog.HAS_CYCLE_STATUS_UPDATE)
    @PatchMapping("/accreditation-cycles/{id}/status")
    public ApiResponse<AccreditationCycleResponse> changeStatus(
            @PathVariable UUID id, @Valid @RequestBody AccreditationCycleStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status(), request.remarks()),
                "Accreditation cycle status updated");
    }
}
