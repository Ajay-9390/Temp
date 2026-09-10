package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationInput;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.calculation.COContribution;
import com.nba.attainment.dto.domain.COAttainmentData;
import com.nba.attainment.dto.domain.COMappingData;
import com.nba.attainment.dto.domain.ProgramSpecificOutcomeData;
import com.nba.attainment.exception.InvalidAttainmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates PSO attainment for a set of program specific outcomes.
 *
 * <p>Mirrors {@link POAttainmentCalculator} in structure but operates on
 * {@link ProgramSpecificOutcomeData} and CO→PSO mappings. The mathematical
 * formula is shared via the injected {@link AttainmentCalculationStrategy}.
 *
 * <p>No Spring, JPA, or I/O dependencies. Fully testable with plain JUnit.
 */
public class PSOAttainmentCalculator {

    private static final Logger log = LoggerFactory.getLogger(PSOAttainmentCalculator.class);

    private final AttainmentCalculationStrategy strategy;

    public PSOAttainmentCalculator(AttainmentCalculationStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy must not be null");
        this.strategy = strategy;
    }

    /**
     * Calculates attainment for every PSO in {@code programSpecificOutcomes}.
     *
     * @param programSpecificOutcomes all PSOs to calculate
     * @param coAttainments           all CO attainments for the program+year
     * @param coPsoMappings           all CO→PSO mappings for the program
     * @param programId               the program being calculated
     * @param academicYear            e.g. "2023-24"
     * @param calculationVersion      e.g. "v1"
     * @return one result per PSO; order matches {@code programSpecificOutcomes}
     */
    public List<AttainmentCalculationResult> calculateAll(
            List<ProgramSpecificOutcomeData> programSpecificOutcomes,
            List<COAttainmentData>           coAttainments,
            List<COMappingData>              coPsoMappings,
            UUID                             programId,
            String                           academicYear,
            String                           calculationVersion) {

        validateInputs(programId, academicYear, coAttainments);

        // Index CO attainments by coId for O(1) lookup
        Map<UUID, COAttainmentData> attainmentByCoId = coAttainments.stream()
                .collect(Collectors.toMap(COAttainmentData::coId, Function.identity(),
                        (a, b) -> {
                            log.warn("Duplicate CO attainment for coId={}, using first", a.coId());
                            return a;
                        }));

        // Group mappings by psoId
        Map<UUID, List<COMappingData>> mappingsByPsoId = coPsoMappings.stream()
                .filter(COMappingData::isPSO)
                .collect(Collectors.groupingBy(COMappingData::psoId));

        List<AttainmentCalculationResult> results = new ArrayList<>();

        for (ProgramSpecificOutcomeData pso : programSpecificOutcomes) {
            List<COMappingData> psoMappings =
                    mappingsByPsoId.getOrDefault(pso.psoId(), List.of());

            if (psoMappings.isEmpty()) {
                log.warn("No CO→PSO mappings found for PSO {} ({}). Attainment will be 0.",
                         pso.code(), pso.psoId());
            }

            List<COContribution> contributions = buildContributions(
                    pso.psoId(), pso.code(), psoMappings, attainmentByCoId);

            AttainmentCalculationInput input = new AttainmentCalculationInput(
                    pso.psoId(), pso.code(), programId, academicYear,
                    contributions, strategy.strategyName());

            log.debug("Calculating PSO {} with {} contributions using strategy {}",
                      pso.code(), contributions.size(), strategy.strategyName());

            AttainmentCalculationResult result = strategy.calculate(input);
            results.add(result);

            log.info("PSO {} ({}) = {:.2f}%", pso.code(), pso.psoId(),
                     result.directAttainment());
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<COContribution> buildContributions(
            UUID psoId, String psoCode,
            List<COMappingData> mappings,
            Map<UUID, COAttainmentData> attainmentByCoId) {

        List<COContribution> contributions = new ArrayList<>();

        for (COMappingData mapping : mappings) {
            COAttainmentData attainment = attainmentByCoId.get(mapping.coId());
            if (attainment == null) {
                log.warn("No CO attainment found for coId={} (mapped to PSO {}). Skipping.",
                         mapping.coId(), psoCode);
                continue;
            }
            contributions.add(new COContribution(
                    mapping.coId(),
                    mapping.coCode(),
                    attainment.courseId(),
                    attainment.courseName(),
                    attainment.attainment(),
                    mapping.mappingLevel()));
        }

        return contributions;
    }

    private void validateInputs(UUID programId, String academicYear,
                                 List<COAttainmentData> coAttainments) {
        if (programId == null) {
            throw new InvalidAttainmentException("programId must not be null");
        }
        if (academicYear == null || academicYear.isBlank()) {
            throw new InvalidAttainmentException("academicYear must not be blank");
        }
        for (COAttainmentData co : coAttainments) {
            if (!programId.equals(co.programId())) {
                throw new InvalidAttainmentException(
                        "CO attainment programId mismatch: expected " + programId +
                        " but got " + co.programId() + " for coId=" + co.coId());
            }
        }
    }
}
