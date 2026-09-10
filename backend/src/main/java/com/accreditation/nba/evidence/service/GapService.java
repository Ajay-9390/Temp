package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.dto.response.GapItemResponse;
import com.accreditation.nba.evidence.dto.response.GapReportResponse;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.integration.EvidenceRequirementProvider;
import com.accreditation.nba.evidence.integration.RequiredEvidence;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.view.RequirementCoverageView;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic evidence gap detection: compares the required evidence for a program + academic year
 * (supplied by the external {@link EvidenceRequirementProvider}) against the evidence actually
 * present (via explicit mappings and the evidence's own primary criterion/requirement).
 *
 * <p>This module does not create or manage NBA criteria — it only consumes their IDs.
 */
@Service
public class GapService {

    private static final String MISSING = "MISSING";

    private static final Map<EvidenceStatus, Integer> STATUS_RANK = new EnumMap<>(EvidenceStatus.class);

    static {
        STATUS_RANK.put(EvidenceStatus.APPROVED, 6);
        STATUS_RANK.put(EvidenceStatus.UNDER_REVIEW, 5);
        STATUS_RANK.put(EvidenceStatus.SUBMITTED, 4);
        STATUS_RANK.put(EvidenceStatus.CHANGES_REQUIRED, 3);
        STATUS_RANK.put(EvidenceStatus.DRAFT, 2);
        STATUS_RANK.put(EvidenceStatus.REJECTED, 1);
        STATUS_RANK.put(EvidenceStatus.ARCHIVED, 0);
    }

    private final EvidenceRequirementProvider requirementProvider;
    private final NbaEvidenceMappingRepository mappingRepository;
    private final NbaEvidenceRepository evidenceRepository;

    public GapService(EvidenceRequirementProvider requirementProvider,
                      NbaEvidenceMappingRepository mappingRepository,
                      NbaEvidenceRepository evidenceRepository) {
        this.requirementProvider = requirementProvider;
        this.mappingRepository = mappingRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional(readOnly = true)
    public GapReportResponse computeGap(UUID programId, UUID academicYearId, UUID criterionId, String statusFilter) {
        List<RequiredEvidence> required = requirementProvider.getRequiredEvidence(programId, academicYearId);
        Map<Key, Coverage> coverageByRequirement = buildCoverage(programId, academicYearId);

        List<GapItemResponse> allItems = new ArrayList<>();
        long approved = 0;
        long missing = 0;
        long rejected = 0;
        long uploaded = 0;

        for (RequiredEvidence req : required) {
            if (criterionId != null && !criterionId.equals(req.criterionId())) {
                continue;
            }
            Coverage coverage = coverageByRequirement.get(new Key(req.criterionId(), req.requirementId()));
            String currentStatus = coverage == null ? MISSING : coverage.status().name();
            boolean satisfied = coverage != null && coverage.status() == EvidenceStatus.APPROVED;
            UUID evidenceId = coverage == null ? null : coverage.evidenceId();

            if (coverage == null) {
                missing++;
            } else {
                uploaded++;
                if (coverage.status() == EvidenceStatus.APPROVED) {
                    approved++;
                } else if (coverage.status() == EvidenceStatus.REJECTED) {
                    rejected++;
                }
            }

            allItems.add(new GapItemResponse(
                    req.criterionId(), req.requirementId(), req.criterionCode(), req.requirementCode(),
                    req.expectedCategory(), req.expectedEvidenceName(), req.mandatory(),
                    currentStatus, evidenceId, satisfied));
        }

        long totalRequired = allItems.size();
        long pending = uploaded - approved - rejected;

        List<GapItemResponse> displayed = allItems.stream()
                .filter(item -> statusFilter == null || statusFilter.isBlank()
                        || statusFilter.equalsIgnoreCase(item.currentStatus()))
                .toList();

        return new GapReportResponse(programId, academicYearId, totalRequired, uploaded, approved,
                pending, rejected, missing, displayed);
    }

    private Map<Key, Coverage> buildCoverage(UUID programId, UUID academicYearId) {
        Map<Key, Coverage> best = new HashMap<>();
        List<RequirementCoverageView> views = new ArrayList<>();
        views.addAll(mappingRepository.findMappedCoverage(programId, academicYearId));
        views.addAll(evidenceRepository.findPrimaryCoverage(programId, academicYearId));

        for (RequirementCoverageView view : views) {
            if (view.getCriterionId() == null || view.getRequirementId() == null || view.getStatus() == null) {
                continue;
            }
            Key key = new Key(view.getCriterionId(), view.getRequirementId());
            Coverage candidate = new Coverage(view.getEvidenceId(), view.getStatus());
            best.merge(key, candidate, (existing, incoming) ->
                    rank(incoming.status()) > rank(existing.status()) ? incoming : existing);
        }
        return best;
    }

    private int rank(EvidenceStatus status) {
        return STATUS_RANK.getOrDefault(status, 0);
    }

    private record Key(UUID criterionId, UUID requirementId) {
    }

    private record Coverage(UUID evidenceId, EvidenceStatus status) {
    }
}
