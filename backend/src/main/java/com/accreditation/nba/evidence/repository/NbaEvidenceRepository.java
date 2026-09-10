package com.accreditation.nba.evidence.repository;

import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.repository.view.CategoryCountView;
import com.accreditation.nba.evidence.repository.view.RequirementCoverageView;
import com.accreditation.nba.evidence.repository.view.StatusCountView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for the root evidence aggregate. {@link JpaSpecificationExecutor} powers the
 * dynamic search/filter API (see {@code EvidenceSpecifications}).
 */
public interface NbaEvidenceRepository
        extends JpaRepository<NbaEvidence, UUID>, JpaSpecificationExecutor<NbaEvidence> {

    @Query("""
            select e.status as status, count(e) as count
            from NbaEvidence e
            where (:programId is null or e.programId = :programId)
              and (:academicYearId is null or e.academicYearId = :academicYearId)
            group by e.status
            """)
    List<StatusCountView> countGroupedByStatus(@Param("programId") UUID programId,
                                               @Param("academicYearId") UUID academicYearId);

    @Query("""
            select e.category as category, count(e) as count
            from NbaEvidence e
            where (:programId is null or e.programId = :programId)
              and (:academicYearId is null or e.academicYearId = :academicYearId)
            group by e.category
            """)
    List<CategoryCountView> countGroupedByCategory(@Param("programId") UUID programId,
                                                    @Param("academicYearId") UUID academicYearId);

    /**
     * Coverage derived from the evidence's own primary criterion/requirement fields.
     */
    @Query("""
            select e.criterionId as criterionId, e.requirementId as requirementId,
                   e.id as evidenceId, e.status as status
            from NbaEvidence e
            where e.criterionId is not null and e.requirementId is not null
              and (:programId is null or e.programId = :programId)
              and (:academicYearId is null or e.academicYearId = :academicYearId)
            """)
    List<RequirementCoverageView> findPrimaryCoverage(@Param("programId") UUID programId,
                                                       @Param("academicYearId") UUID academicYearId);
}
