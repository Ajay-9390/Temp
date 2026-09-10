package com.example.nba.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the fallback {@link DataScopeService}. Uses {@code @ConditionalOnMissingBean} on a
 * {@code @Bean} method (the only reliable place for it) so the central RBAC team's real
 * implementation — provided as any {@link DataScopeService} bean — automatically takes over.
 */
@Configuration
public class DataScopeConfig {

    @Bean
    @ConditionalOnMissingBean(DataScopeService.class)
    public DataScopeService permissiveDataScopeService() {
        return new PermissiveDataScopeService();
    }
}
