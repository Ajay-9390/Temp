package com.accreditation.nba.evidence.repository;

import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NbaEvidenceVersionRepository extends JpaRepository<NbaEvidenceVersion, UUID> {

    List<NbaEvidenceVersion> findByEvidenceIdOrderByVersionNumberDesc(UUID evidenceId);

    Optional<NbaEvidenceVersion> findByEvidenceIdAndVersionNumber(UUID evidenceId, int versionNumber);

    Optional<NbaEvidenceVersion> findTopByEvidenceIdOrderByVersionNumberDesc(UUID evidenceId);

    long countByEvidenceId(UUID evidenceId);

    boolean existsByChecksum(String checksum);

    List<NbaEvidenceVersion> findByChecksum(String checksum);
}
