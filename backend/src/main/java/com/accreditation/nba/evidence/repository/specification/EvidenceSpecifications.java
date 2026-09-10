package com.accreditation.nba.evidence.repository.specification;

import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds dynamic JPA {@link Specification}s for evidence search/filtering. All criteria are
 * optional and combined with AND. Keyword matching spans evidence title/description/tags and
 * (via a correlated sub-query) the version file name and extracted text.
 *
 * <p>This is the portable baseline; it can later be swapped for PostgreSQL full-text search
 * using the {@code search_vector} columns added in migration V2 (see {@code SearchService}).
 */
public final class EvidenceSpecifications {

    private EvidenceSpecifications() {
    }

    public static Specification<NbaEvidence> fromCriteria(EvidenceSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (c.programId() != null) {
                predicates.add(cb.equal(root.get("programId"), c.programId()));
            }
            if (c.departmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), c.departmentId()));
            }
            if (c.academicYearId() != null) {
                predicates.add(cb.equal(root.get("academicYearId"), c.academicYearId()));
            }
            if (c.criterionId() != null) {
                predicates.add(cb.equal(root.get("criterionId"), c.criterionId()));
            }
            if (c.requirementId() != null) {
                predicates.add(cb.equal(root.get("requirementId"), c.requirementId()));
            }
            if (c.category() != null) {
                predicates.add(cb.equal(root.get("category"), c.category()));
            }
            if (c.status() != null) {
                predicates.add(cb.equal(root.get("status"), c.status()));
            }
            if (c.uploadedBy() != null && !c.uploadedBy().isBlank()) {
                predicates.add(cb.equal(root.get("uploadedBy"), c.uploadedBy()));
            }
            if (c.createdFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), c.createdFrom()));
            }
            if (c.createdTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), c.createdTo()));
            }

            if (c.fileType() != null && !c.fileType().isBlank()) {
                predicates.add(cb.exists(versionExists(root, query, cb,
                        (vRoot, versionCb) -> versionCb.equal(
                                versionCb.lower(vRoot.get("fileType")),
                                c.fileType().toLowerCase()))));
            }

            if (c.keyword() != null && !c.keyword().isBlank()) {
                String like = "%" + c.keyword().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), like);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), like);
                Predicate tagsMatch = cb.like(cb.lower(root.get("tagsRaw")), like);
                Predicate fileMatch = cb.exists(versionExists(root, query, cb,
                        (vRoot, versionCb) -> versionCb.or(
                                versionCb.like(versionCb.lower(vRoot.get("fileName")), like),
                                versionCb.like(versionCb.lower(vRoot.get("extractedText")), like))));
                predicates.add(cb.or(titleMatch, descMatch, tagsMatch, fileMatch));
            }

            // De-duplicate when the query joins/sub-queries produce multiplicity.
            if (query != null) {
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Correlated EXISTS sub-query over versions of the current evidence row.
     */
    private static Subquery<Integer> versionExists(
            Root<NbaEvidence> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            VersionPredicate versionPredicate) {

        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<NbaEvidenceVersion> version = sub.from(NbaEvidenceVersion.class);
        sub.select(cb.literal(1));
        sub.where(
                cb.equal(version.get("evidenceId"), root.get("id")),
                versionPredicate.build(version, cb));
        return sub;
    }

    @FunctionalInterface
    private interface VersionPredicate {
        Predicate build(Root<NbaEvidenceVersion> versionRoot,
                        jakarta.persistence.criteria.CriteriaBuilder cb);
    }
}
