package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationInput;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.calculation.COContribution;
import com.nba.attainment.dto.domain.COAttainmentData;
import com.nba.attainment.dto.domain.COMappingData;
import com.nba.attainment.dto.domain.ProgramOutcomeData;
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
 * Calculates PO attainment for a set of program outcomes.
 *
 * <h2>Responsibility</h2>
 * <ol>
 *   <li>Accept pre-fetched domain data (COs, mappings, PO definitions)</li>
 *   <li>Build {@link AttainmentCalculationInput} for each PO</li>
 *   <li>Delegate the mathematical formula to the configured strategy</li>
 *   <li>Collect and return results</li>
 * </ol>
 *
 * <h2>What this class does NOT do</h2>
 * <ul>
 *   <li>Access any JPA repository</li>
 *   <li>Call any HTTP endpoint</li>
 *   <li>Interact with Spring MVC</li>
 *   <li>Call any provider directly</li>
 * </ul>
 *
 * <p>Instantiate with {@code new POAttainmentCalculator(strategy)} in unit tests —
 * no Spring context required.
 */
public class POAttainmentCalculator {

    private static final Logger log = LoggerFactory.getLogger(POAttainmentCalculator.class);

    private final AttainmentCalculationStrategy strategy;

    public POAttainmentCalculator(AttainmentCalculationStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy must not be null");
        this.strategy = strategy;
    }

    /**
     * Calculates attainment for every PO in {@code programOutcomes}.
     *
     * @param programOutcomes   all POs to calculate (already loaded by the application service)
     * @param coAttainments     all CO attainments for the program+year
     * @param coPoMappings      all CO→PO mappings for the program
     * @param programId         the program being calculated
     * @param academicYear      e.g. "2023-24"
     * @param calculationVersion  e.g. "v1"
     * @return one result per PO; order matches {@code programOutcomes}
     */
    public List<AttainmentCalculationResult> calculateAll(
            List<ProgramOutcomeData>  programOutcomes,
            List<COAttainmentData>    coAttainments,
            List<COMappingData>       coPoMappings,
            UUID                      programId,
            String                    academicYear,
            String                    calculationVersion) {

        validateInputs(programId, academicYear, coAttainments);

        // Index CO attainments by coId for O(1) lookup
        Map<UUID, COAttainmentData> attainmentByCoId = coAttainments.stream()
                .collect(Collectors.toMap(COAttainmentData::coId, Function.identity(),
                        (a, b) -> {
                            log.warn("Duplicate CO attainment for coId={}, using first", a.coId());
                            return a;
                        }));

        // Group mappings by poId
        Map<UUID, List<COMappingData>> mappingsByPoId = coPoMappings.stream()
                .filter(COMappingData::isToPO)
                .collect(Collectors.groupingBy(COMappingData::poId));

        List<AttainmentCalculationResult> results = new ArrayList<>();

        for (ProgramOutcomeData po : programOutcomes) {
            List<COMappingData> poMappings = mappingsByPoId.getOrDefault(po.poId(), List.of());

            if (poMappings.isEmpty()) {
                log.warn("No CO→PO mappings found for PO {} ({}). Attainment will be 0.",
                         po.code(), po.poId());
            }

            List<COContribution> contributions = buildContributions(
                    po.poId(), po.code(), poMappings, attainmentByCoId);

            AttainmentCalculationInput input = new AttainmentCalculationInput(
                    po.poId(), po.code(), programId, academicYear,
                    contributions, strategy.strategyName());

            log.debug("Calculating PO {} with {} contributions using strategy {}",
                      po.code(), contributions.size(), strategy.strategyName());

            AttainmentCalculationResult result = strategy.calculate(input);
            results.add(result);

            log.info("PO {} ({}) = {:.2f}%", po.code(), po.poId(),
                     result.directAttainment());
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<COContribution> buildContributions(
            UUID poId, String poCode,
            List<COMappingData> mappings,
            Map<UUID, COAttainmentData> attainmentByCoId) {

        List<COContribution> contributions = new ArrayList<>();

        for (COMappingData mapping : mappings) {
            COAttainmentData attainment = attainmentByCoId.get(mapping.coId());
            if (attainment == null) {
                log.warn("No CO attainment found for coId={} (mapped to PO {}). Skipping.",
                         mapping.coId(), poCode);
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
            if (co.attainment() < 0.0 || co.attainment() > 100.0) {
                throw new InvalidAttainmentException(
                        "CO attainment out of range [0, 100]: " + co.attainment() +
                        " for coId=" + co.coId());
            }
        }
    }
}
