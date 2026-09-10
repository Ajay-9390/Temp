package com.example.nba.institution.controller;

import com.example.nba.common.response.ApiResponse;
import com.example.nba.common.response.PageResponse;
import com.example.nba.institution.dto.InstitutionRequest;
import com.example.nba.institution.dto.InstitutionResponse;
import com.example.nba.institution.dto.InstitutionStatusRequest;
import com.example.nba.institution.entity.InstitutionStatus;
import com.example.nba.institution.service.InstitutionService;
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

@Tag(name = "Institutions", description = "Institution reference management")
@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService service;

    @Operation(summary = "List institutions", description = "Requires INSTITUTION_VIEW. Supports search, status filter, pagination, sorting.")
    @PreAuthorize(PermissionCatalog.HAS_INSTITUTION_VIEW)
    @GetMapping
    public ApiResponse<PageResponse<InstitutionResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InstitutionStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(search, status, pageable)));
    }

    @Operation(summary = "Get institution by id", description = "Requires INSTITUTION_VIEW.")
    @PreAuthorize(PermissionCatalog.HAS_INSTITUTION_VIEW)
    @GetMapping("/{id}")
    public ApiResponse<InstitutionResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Create institution", description = "Requires INSTITUTION_CREATE.")
    @PreAuthorize(PermissionCatalog.HAS_INSTITUTION_CREATE)
    @PostMapping
    public ResponseEntity<ApiResponse<InstitutionResponse>> create(@Valid @RequestBody InstitutionRequest request) {
        InstitutionResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Institution created successfully"));
    }

    @Operation(summary = "Update institution", description = "Requires INSTITUTION_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_INSTITUTION_UPDATE)
    @PutMapping("/{id}")
    public ApiResponse<InstitutionResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody InstitutionRequest request) {
        return ApiResponse.ok(service.update(id, request), "Institution updated successfully");
    }

    @Operation(summary = "Change institution status", description = "Requires INSTITUTION_STATUS_UPDATE.")
    @PreAuthorize(PermissionCatalog.HAS_INSTITUTION_STATUS_UPDATE)
    @PatchMapping("/{id}/status")
    public ApiResponse<InstitutionResponse> changeStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody InstitutionStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request.status()), "Institution status updated");
    }
}
