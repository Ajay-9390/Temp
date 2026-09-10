package com.accreditation.nba.evidence.ai;

import com.accreditation.nba.evidence.entity.NbaEvidence;
import java.util.List;
import java.util.UUID;

/**
 * Default no-op AI implementation. Registered as the default {@link AiEvidenceService} bean
 * in {@code DefaultIntegrationConfig} via {@code @ConditionalOnMissingBean}, so the module
 * functions fully without any AI provider and a real implementation replaces it when added.
 */
public class NoOpAiEvidenceService implements AiEvidenceService {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public List<EvidenceCategorySuggestion> suggestCategories(NbaEvidence evidence, String extractedText) {
        return List.of();
    }

    @Override
    public List<MappingSuggestion> suggestMappings(NbaEvidence evidence, String extractedText) {
        return List.of();
    }

    @Override
    public List<UUID> findPotentialDuplicates(NbaEvidence evidence, String checksum, String extractedText) {
        return List.of();
    }

    @Override
    public String summarize(String extractedText) {
        return null;
    }
}
