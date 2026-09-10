package com.accreditation.nba.evidence.report;

import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.dto.response.GapItemResponse;
import com.accreditation.nba.evidence.dto.response.GapReportResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.service.GapService;
import com.accreditation.nba.evidence.service.SearchService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds evidence-related reports (completion + status) and delegates rendering to
 * {@link ReportExporter} (XLSX / PDF / CSV). Only evidence-scoped reports are produced here.
 */
@Service
public class ReportService {

    private static final String MISSING = "MISSING";

    private final GapService gapService;
    private final SearchService searchService;
    private final ReportExporter exporter;

    public ReportService(GapService gapService, SearchService searchService, ReportExporter exporter) {
        this.gapService = gapService;
        this.searchService = searchService;
        this.exporter = exporter;
    }

    /**
     * Completion by criterion: required / uploaded / approved / missing / completion %.
     */
    @Transactional(readOnly = true)
    public ReportFile completionReport(UUID programId, UUID academicYearId, String format) {
        GapReportResponse gap = gapService.computeGap(programId, academicYearId, null, null);

        // criterionCode -> [required, uploaded, approved, missing]
        Map<String, long[]> byCriterion = new LinkedHashMap<>();
        for (GapItemResponse item : gap.items()) {
            long[] counts = byCriterion.computeIfAbsent(item.criterionCode(), k -> new long[4]);
            counts[0]++;
            if (!MISSING.equalsIgnoreCase(item.currentStatus())) {
                counts[1]++;
            }
            if (item.satisfied()) {
                counts[2]++;
            }
            if (MISSING.equalsIgnoreCase(item.currentStatus())) {
                counts[3]++;
            }
        }

        List<String> headers = List.of("Criterion", "Required", "Uploaded", "Approved", "Missing", "Completion %");
        List<List<String>> rows = new ArrayList<>();
        long tReq = 0;
        long tUp = 0;
        long tApp = 0;
        long tMiss = 0;
        for (Map.Entry<String, long[]> entry : byCriterion.entrySet()) {
            long[] c = entry.getValue();
            tReq += c[0];
            tUp += c[1];
            tApp += c[2];
            tMiss += c[3];
            rows.add(List.of(
                    nullSafe(entry.getKey()),
                    String.valueOf(c[0]),
                    String.valueOf(c[1]),
                    String.valueOf(c[2]),
                    String.valueOf(c[3]),
                    percent(c[2], c[0])));
        }
        rows.add(List.of("TOTAL", String.valueOf(tReq), String.valueOf(tUp),
                String.valueOf(tApp), String.valueOf(tMiss), percent(tApp, tReq)));

        String title = "Evidence Completion Report (program=" + programId + ", academicYear=" + academicYearId + ")";
        return exporter.export(format, "evidence-completion-report", title, headers, rows);
    }

    /**
     * Status listing of matching evidence.
     */
    @Transactional(readOnly = true)
    public ReportFile statusReport(EvidenceSearchCriteria criteria, String format) {
        List<NbaEvidence> evidences = searchService.search(criteria, Pageable.unpaged()).getContent();

        List<String> headers = List.of("Title", "Category", "Program", "Academic Year",
                "Criterion", "Requirement", "Status", "Version", "Uploaded By", "Created At");
        List<List<String>> rows = new ArrayList<>();
        for (NbaEvidence e : evidences) {
            rows.add(List.of(
                    nullSafe(e.getTitle()),
                    String.valueOf(e.getCategory()),
                    nullSafe(Objects.toString(e.getProgramId(), "")),
                    nullSafe(Objects.toString(e.getAcademicYearId(), "")),
                    nullSafe(Objects.toString(e.getCriterionId(), "")),
                    nullSafe(Objects.toString(e.getRequirementId(), "")),
                    String.valueOf(e.getStatus()),
                    String.valueOf(e.getCurrentVersion()),
                    nullSafe(e.getUploadedBy()),
                    Objects.toString(e.getCreatedAt(), "")));
        }

        return exporter.export(format, "evidence-status-report", "Evidence Status Report", headers, rows);
    }

    private String percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0.0%";
        }
        return String.format("%.1f%%", (numerator * 100.0) / denominator);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
