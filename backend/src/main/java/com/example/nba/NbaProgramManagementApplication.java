package com.example.nba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the NBA Program Management module.
 *
 * <p>Packaged as a modular monolith slice: it can run standalone today and be merged into
 * the larger NBA platform later without restructuring. It owns Institution, Department,
 * Program, AccreditationCycle, AcademicYear and Semester and exposes stable UUID
 * identifiers for future modules (Courses, Outcomes, SAR, etc.).</p>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
public class NbaProgramManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(NbaProgramManagementApplication.class, args);
    }
}
