package com.accreditation.nba.evidence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * NBA Evidence Management module.
 *
 * <p>Owns the complete lifecycle of NBA accreditation evidence (create, upload, validate,
 * store, map, review, version, audit, search, gap detection, statistics, reports).
 *
 * <p>This module references other modules (Program, Department, Academic Year, NBA Criteria)
 * by ID only and never re-creates their tables. Authentication is intentionally absent in
 * this development phase; a mock identity is provided via {@code CurrentUserProvider}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableCaching
public class EvidenceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvidenceManagementApplication.class, args);
    }
}
