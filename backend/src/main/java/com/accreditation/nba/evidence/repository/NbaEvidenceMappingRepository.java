package com.accreditation.nba.evidence.repository;

import com.accreditation.nba.evidence.entity.NbaEvidenceMapping;
import com.accreditation.nba.evidence.repository.view.RequirementCoverageView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NbaEvidenceMappingRepository extends JpaRepository<NbaEvidenceMapping, UUID> {

    List<NbaEvidenceMapping> findByEvidenceId(UUID evidenceId);

    Optional<NbaEvidenceMapping> findByIdAndEvidenceId(UUID id, UUID evidenceId);

    boolean existsByEvidenceIdAndCriterionIdAndRequirementId(UUID evidenceId, UUID criterionId, UUID requirementId);

    long countByEvidenceId(UUID evidenceId);

    void deleteByEvidenceId(UUID evidenceId);

    /**
     * Coverage derived from explicit mappings (theta join to the evidence to filter by
     * program / academic year and read the evidence status).
     */
    @Query("""
            select m.criterionId as criterionId, m.requirementId as requirementId,
                   e.id as evidenceId, e.status as status
            from NbaEvidenceMapping m, NbaEvidence e
            where e.id = m.evidenceId
              and (:programId is null or e.programId = :programId)
              and (:academicYearId is null or e.academicYearId = :academicYearId)
            """)
    List<RequirementCoverageView> findMappedCoverage(@Param("programId") UUID programId,
                                                      @Param("academicYearId") UUID academicYearId);
}
