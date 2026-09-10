package com.accreditation.nba.evidence.config;

import com.accreditation.nba.evidence.ai.AiEvidenceService;
import com.accreditation.nba.evidence.ai.NoOpAiEvidenceService;
import com.accreditation.nba.evidence.integration.EvidenceRequirementProvider;
import com.accreditation.nba.evidence.integration.MockEvidenceRequirementProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the module's default integration beans. Using {@code @ConditionalOnMissingBean}
 * on {@code @Bean} methods (the supported pattern) means real implementations added later —
 * e.g. an NBA Criteria REST client or a Spring AI service — automatically replace these
 * defaults without any code change here.
 */
@Configuration
public class DefaultIntegrationConfig {

    @Bean
    @ConditionalOnMissingBean(EvidenceRequirementProvider.class)
    public EvidenceRequirementProvider evidenceRequirementProvider() {
        return new MockEvidenceRequirementProvider();
    }

    @Bean
    @ConditionalOnMissingBean(AiEvidenceService.class)
    public AiEvidenceService aiEvidenceService() {
        return new NoOpAiEvidenceService();
    }
}
