package com.accreditation.nba.evidence.repository;

import com.accreditation.nba.evidence.entity.NbaEvidenceAudit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NbaEvidenceAuditRepository extends JpaRepository<NbaEvidenceAudit, UUID> {

    List<NbaEvidenceAudit> findByEvidenceIdOrderByCreatedAtDesc(UUID evidenceId);
}
