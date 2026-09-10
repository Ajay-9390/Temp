package com.accreditation.nba.evidence.ai;

import com.accreditation.nba.evidence.entity.NbaEvidence;
import java.util.List;
import java.util.UUID;

/**
 * Extension point for future AI-assisted capabilities (Spring AI + pgvector): semantic
 * search, auto-classification, suggested criterion mapping, duplicate/missing detection and
 * summarisation.
 *
 * <p>AI is never required for basic upload/review and must never auto-approve evidence —
 * human review remains the final decision. The default {@link NoOpAiEvidenceService}
 * returns empty results so the module runs without any AI provider configured.
 */
public interface AiEvidenceService {

    boolean isEnabled();

    /** Suggested categories for an evidence item, most likely first. */
    List<EvidenceCategorySuggestion> suggestCategories(NbaEvidence evidence, String extractedText);

    /** Suggested criterion/requirement mappings (advisory only, never auto-applied). */
    List<MappingSuggestion> suggestMappings(NbaEvidence evidence, String extractedText);

    /** IDs of evidence that appear to duplicate the given one (e.g. by embedding similarity). */
    List<UUID> findPotentialDuplicates(NbaEvidence evidence, String checksum, String extractedText);

    /** Short summary of the evidence content. */
    String summarize(String extractedText);

    record EvidenceCategorySuggestion(String category, double confidence) {
    }

    record MappingSuggestion(UUID criterionId, UUID requirementId, double confidence, String rationale) {
    }
}
