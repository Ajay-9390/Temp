package com.accreditation.nba.evidence.mapper;

import com.accreditation.nba.evidence.dto.response.AuditResponse;
import com.accreditation.nba.evidence.dto.response.EvidenceResponse;
import com.accreditation.nba.evidence.dto.response.EvidenceSummaryResponse;
import com.accreditation.nba.evidence.dto.response.MappingResponse;
import com.accreditation.nba.evidence.dto.response.ReviewResponse;
import com.accreditation.nba.evidence.dto.response.VersionResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceAudit;
import com.accreditation.nba.evidence.entity.NbaEvidenceMapping;
import com.accreditation.nba.evidence.entity.NbaEvidenceReview;
import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import org.springframework.stereotype.Component;

/**
 * Hand-written entity &rarr; DTO mapping. Kept explicit (no framework) so the wire contract
 * is obvious and controllers never leak JPA entities.
 */
@Component
public class EvidenceMapper {

    private static final int TEXT_PREVIEW_LENGTH = 500;

    public VersionResponse toVersionResponse(NbaEvidenceVersion v) {
        if (v == null) {
            return null;
        }
        String text = v.getExtractedText();
        boolean hasText = text != null && !text.isBlank();
        String preview = hasText
                ? text.substring(0, Math.min(text.length(), TEXT_PREVIEW_LENGTH))
                : null;
        return new VersionResponse(
                v.getId(),
                v.getEvidenceId(),
                v.getVersionNumber(),
                v.getFileName(),
                v.getFileType(),
                v.getMimeType(),
                v.getFileSize(),
                v.getChecksum(),
                v.getUploadedBy(),
                v.getChangeReason(),
                v.getPageCount(),
                v.getDetectedLanguage(),
                v.getOcrStatus(),
                v.getOcrProcessedAt(),
                hasText,
                preview,
                v.getCreatedAt());
    }

    public EvidenceResponse toEvidenceResponse(NbaEvidence e,
                                               long versionCount,
                                               long mappingCount,
                                               long reviewCount,
                                               NbaEvidenceVersion latestVersion) {
        return new EvidenceResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getCategory(),
                e.getProgramId(),
                e.getDepartmentId(),
                e.getAcademicYearId(),
                e.getCriterionId(),
                e.getRequirementId(),
                e.getStatus(),
                e.getCurrentVersion(),
                e.getTags(),
                e.getUploadedBy(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                versionCount,
                mappingCount,
                reviewCount,
                toVersionResponse(latestVersion));
    }

    public EvidenceSummaryResponse toSummary(NbaEvidence e, String latestFileType) {
        return new EvidenceSummaryResponse(
                e.getId(),
                e.getTitle(),
                e.getCategory(),
                e.getProgramId(),
                e.getAcademicYearId(),
                e.getCriterionId(),
                e.getRequirementId(),
                e.getStatus(),
                e.getCurrentVersion(),
                latestFileType,
                e.getUploadedBy(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public MappingResponse toMappingResponse(NbaEvidenceMapping m) {
        return new MappingResponse(
                m.getId(),
                m.getEvidenceId(),
                m.getCriterionId(),
                m.getRequirementId(),
                m.getMappingType(),
                m.getCreatedBy(),
                m.getCreatedAt());
    }

    public ReviewResponse toReviewResponse(NbaEvidenceReview r) {
        return new ReviewResponse(
                r.getId(),
                r.getEvidenceId(),
                r.getVersionNumber(),
                r.getReviewer(),
                r.getDecision(),
                r.getResultingStatus(),
                r.getComments(),
                r.getReviewedAt());
    }

    public AuditResponse toAuditResponse(NbaEvidenceAudit a) {
        return new AuditResponse(
                a.getId(),
                a.getEvidenceId(),
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getPerformedBy(),
                a.getOldValue(),
                a.getNewValue(),
                a.getReason(),
                a.getCreatedAt());
    }
}
