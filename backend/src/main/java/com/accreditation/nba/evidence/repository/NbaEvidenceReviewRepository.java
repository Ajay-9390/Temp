package com.accreditation.nba.evidence.repository;

import com.accreditation.nba.evidence.entity.NbaEvidenceReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NbaEvidenceReviewRepository extends JpaRepository<NbaEvidenceReview, UUID> {

    List<NbaEvidenceReview> findByEvidenceIdOrderByReviewedAtDesc(UUID evidenceId);

    long countByEvidenceId(UUID evidenceId);
}
