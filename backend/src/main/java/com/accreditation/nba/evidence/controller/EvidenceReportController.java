package com.accreditation.nba.evidence.controller;

import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.report.ReportFile;
import com.accreditation.nba.evidence.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evidence Reports", description = "Completion and status reports (xlsx / pdf / csv)")
@RestController
@RequestMapping("/api/v1/evidences/reports")
public class EvidenceReportController {

    private final ReportService reportService;

    public EvidenceReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Evidence completion report by criterion")
    @GetMapping("/completion")
    public ResponseEntity<byte[]> completion(@RequestParam(required = false) UUID programId,
                                             @RequestParam(required = false) UUID academicYearId,
                                             @RequestParam(defaultValue = "xlsx") String format) {
        return asDownload(reportService.completionReport(programId, academicYearId, format));
    }

    @Operation(summary = "Evidence status report for matching evidence")
    @GetMapping("/status")
    public ResponseEntity<byte[]> status(@RequestParam(required = false) UUID programId,
                                         @RequestParam(required = false) UUID departmentId,
                                         @RequestParam(required = false) UUID academicYearId,
                                         @RequestParam(required = false) UUID criterionId,
                                         @RequestParam(required = false) UUID requirementId,
                                         @RequestParam(required = false) EvidenceCategory category,
                                         @RequestParam(required = false) EvidenceStatus status,
                                         @RequestParam(required = false) String fileType,
                                         @RequestParam(defaultValue = "xlsx") String format) {
        EvidenceSearchCriteria criteria = new EvidenceSearchCriteria(programId, departmentId,
                academicYearId, criterionId, requirementId, category, status, fileType, null, null, null, null);
        return asDownload(reportService.statusReport(criteria, format));
    }

    private ResponseEntity<byte[]> asDownload(ReportFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
