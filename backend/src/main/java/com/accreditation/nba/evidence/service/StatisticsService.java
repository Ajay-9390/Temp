package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.config.CacheConfig;
import com.accreditation.nba.evidence.dto.response.GapReportResponse;
import com.accreditation.nba.evidence.dto.response.StatisticsResponse;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.view.CategoryCountView;
import com.accreditation.nba.evidence.repository.view.StatusCountView;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregated evidence statistics for the dashboard. Cacheable (Redis) when
 * {@code spring.cache.type=redis}; works without any cache otherwise. Required/missing counts
 * are derived from gap detection and only computed when a program + academic year are provided.
 */
@Service
public class StatisticsService {

    private final NbaEvidenceRepository evidenceRepository;
    private final GapService gapService;

    public StatisticsService(NbaEvidenceRepository evidenceRepository, GapService gapService) {
        this.evidenceRepository = evidenceRepository;
        this.gapService = gapService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.STATISTICS_CACHE,
            key = "T(java.util.Objects).toString(#programId) + '-' + T(java.util.Objects).toString(#academicYearId)")
    public StatisticsResponse getStatistics(UUID programId, UUID academicYearId) {
        Map<EvidenceStatus, Long> byStatus = new EnumMap<>(EvidenceStatus.class);
        for (EvidenceStatus status : EvidenceStatus.values()) {
            byStatus.put(status, 0L);
        }
        for (StatusCountView view : evidenceRepository.countGroupedByStatus(programId, academicYearId)) {
            byStatus.put(view.getStatus(), view.getCount());
        }

        Map<EvidenceCategory, Long> byCategory = new EnumMap<>(EvidenceCategory.class);
        for (EvidenceCategory category : EvidenceCategory.values()) {
            byCategory.put(category, 0L);
        }
        for (CategoryCountView view : evidenceRepository.countGroupedByCategory(programId, academicYearId)) {
            byCategory.put(view.getCategory(), view.getCount());
        }

        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();

        long required = 0;
        long missing = 0;
        if (programId != null && academicYearId != null) {
            GapReportResponse gap = gapService.computeGap(programId, academicYearId, null, null);
            required = gap.totalRequired();
            missing = gap.missing();
        }

        return new StatisticsResponse(
                programId,
                academicYearId,
                total,
                required,
                byStatus.get(EvidenceStatus.DRAFT),
                byStatus.get(EvidenceStatus.SUBMITTED),
                byStatus.get(EvidenceStatus.UNDER_REVIEW),
                byStatus.get(EvidenceStatus.CHANGES_REQUIRED),
                byStatus.get(EvidenceStatus.APPROVED),
                byStatus.get(EvidenceStatus.REJECTED),
                byStatus.get(EvidenceStatus.ARCHIVED),
                missing,
                byStatus,
                byCategory);
    }
}
