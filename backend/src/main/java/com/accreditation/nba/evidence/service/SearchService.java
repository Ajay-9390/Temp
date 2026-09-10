package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.specification.EvidenceSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evidence search / filtering. Currently backed by portable JPA {@link Specification}s over
 * PostgreSQL (title, description, tags, file name, extracted text + structured filters).
 *
 * <p>Prepared for future upgrades without changing the API:
 * <ul>
 *   <li>PostgreSQL full-text search using the {@code search_vector} columns from migration V2;</li>
 *   <li>pgvector semantic search + AI-based discovery via {@code AiEvidenceService}.</li>
 * </ul>
 */
@Service
public class SearchService {

    private final NbaEvidenceRepository evidenceRepository;

    public SearchService(NbaEvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional(readOnly = true)
    public Page<NbaEvidence> search(EvidenceSearchCriteria criteria, Pageable pageable) {
        EvidenceSearchCriteria effective = criteria != null ? criteria
                : new EvidenceSearchCriteria(null, null, null, null, null, null, null, null, null, null, null, null);
        Specification<NbaEvidence> spec = EvidenceSpecifications.fromCriteria(effective);
        return evidenceRepository.findAll(spec, pageable);
    }
}
