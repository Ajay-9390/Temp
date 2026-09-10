package com.accreditation.nba.evidence.controller;

import com.accreditation.nba.evidence.dto.request.ActionCommentRequest;
import com.accreditation.nba.evidence.dto.request.CreateEvidenceRequest;
import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.dto.request.UpdateEvidenceRequest;
import com.accreditation.nba.evidence.dto.response.EvidenceResponse;
import com.accreditation.nba.evidence.dto.response.EvidenceSummaryResponse;
import com.accreditation.nba.evidence.dto.response.GapReportResponse;
import com.accreditation.nba.evidence.dto.response.PageResponse;
import com.accreditation.nba.evidence.dto.response.StatisticsResponse;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.dto.response.AuditResponse;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.service.EvidenceService;
import com.accreditation.nba.evidence.service.GapService;
import com.accreditation.nba.evidence.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Evidence", description = "Create, read, update, delete and lifecycle actions for NBA evidence")
@RestController
@RequestMapping("/api/v1/evidences")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final StatisticsService statisticsService;
    private final GapService gapService;
    private final EvidenceAuditService auditService;

    public EvidenceController(EvidenceService evidenceService,
                              StatisticsService statisticsService,
                              GapService gapService,
                              EvidenceAuditService auditService) {
        this.evidenceService = evidenceService;
        this.statisticsService = statisticsService;
        this.gapService = gapService;
        this.auditService = auditService;
    }

    @Operation(summary = "Create evidence (metadata only, JSON)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvidenceResponse> create(@Valid @RequestBody CreateEvidenceRequest request) {
        return created(evidenceService.create(request, null));
    }

    @Operation(summary = "Create evidence with an initial file (multipart: 'data' JSON part + 'file')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> createWithFile(
            @Valid @RequestPart("data") CreateEvidenceRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return created(evidenceService.create(request, file));
    }

    @Operation(summary = "List/search evidence with filters and pagination")
    @GetMapping
    public PageResponse<EvidenceSummaryResponse> list(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID academicYearId,
            @RequestParam(required = false) UUID criterionId,
            @RequestParam(required = false) UUID requirementId,
            @RequestParam(required = false) EvidenceCategory category,
            @RequestParam(required = false) EvidenceStatus status,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @PageableDefault(size = 20) Pageable pageable) {
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(programId, departmentId,
                academicYearId, criterionId, requirementId, category, status, fileType, uploadedBy,
                keyword, createdFrom, createdTo);
        return evidenceService.list(criteria, pageable);
    }

    @Operation(summary = "Get evidence by id")
    @GetMapping("/{id}")
    public EvidenceResponse get(@PathVariable UUID id) {
        return evidenceService.get(id);
    }

    @Operation(summary = "Update evidence metadata")
    @PutMapping("/{id}")
    public EvidenceResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateEvidenceRequest request) {
        return evidenceService.update(id, request);
    }

    @Operation(summary = "Delete evidence (only permitted in DRAFT; otherwise archive)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        evidenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Lifecycle ----

    @Operation(summary = "Submit evidence for review (DRAFT/CHANGES_REQUIRED -> SUBMITTED)")
    @PostMapping("/{id}/submit")
    public EvidenceResponse submit(@PathVariable UUID id,
                                   @RequestBody(required = false) ActionCommentRequest request) {
        return evidenceService.submit(id, comment(request));
    }

    @Operation(summary = "Start review (SUBMITTED -> UNDER_REVIEW)")
    @PostMapping("/{id}/start-review")
    public EvidenceResponse startReview(@PathVariable UUID id) {
        return evidenceService.startReview(id);
    }

    @Operation(summary = "Archive evidence")
    @PostMapping("/{id}/archive")
    public EvidenceResponse archive(@PathVariable UUID id,
                                    @RequestBody(required = false) ActionCommentRequest request) {
        return evidenceService.archive(id, comment(request));
    }

    // ---- Analytics ----

    @Operation(summary = "Aggregated evidence statistics for the dashboard")
    @GetMapping("/statistics")
    public StatisticsResponse statistics(@RequestParam(required = false) UUID programId,
                                         @RequestParam(required = false) UUID academicYearId) {
        return statisticsService.getStatistics(programId, academicYearId);
    }

    @Operation(summary = "Evidence gap report (required vs available evidence)")
    @GetMapping("/gaps")
    public GapReportResponse gaps(@RequestParam(required = false) UUID programId,
                                  @RequestParam(required = false) UUID academicYearId,
                                  @RequestParam(required = false) UUID criterionId,
                                  @RequestParam(required = false) String status) {
        return gapService.computeGap(programId, academicYearId, criterionId, status);
    }

    @Operation(summary = "Evidence-scoped business audit trail")
    @GetMapping("/{id}/audit")
    public List<AuditResponse> audit(@PathVariable UUID id) {
        return auditService.getAuditTrail(id);
    }

    private ResponseEntity<EvidenceResponse> created(EvidenceResponse response) {
        return ResponseEntity.created(URI.create("/api/v1/evidences/" + response.id())).body(response);
    }

    private String comment(ActionCommentRequest request) {
        return request == null ? null : request.comment();
    }
}
