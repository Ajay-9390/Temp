package com.nba.attainment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the PO/PSO Attainment module.
 *
 * <p>This module is responsible for:
 * <ul>
 *   <li>Receiving CO attainment data (via providers)</li>
 *   <li>Receiving CO → PO/PSO mapping data (via providers)</li>
 *   <li>Calculating PO and PSO attainment</li>
 *   <li>Persisting results with full calculation traces</li>
 *   <li>Exposing REST APIs for the frontend dashboard</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AttainmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttainmentApplication.class, args);
    }
}
