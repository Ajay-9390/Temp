package com.nba.attainment.service;

import com.nba.attainment.calculation.AttainmentCalculationStrategy;
import com.nba.attainment.calculation.AttainmentStrategyRegistry;
import com.nba.attainment.calculation.POAttainmentCalculator;
import com.nba.attainment.calculation.PSOAttainmentCalculator;
import com.nba.attainment.config.AttainmentProperties;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.domain.*;
import com.nba.attainment.dto.request.*;
import com.nba.attainment.dto.response.*;
import com.nba.attainment.entity.POAttainment;
import com.nba.attainment.entity.PSOAttainment;
import com.nba.attainment.exception.AttainmentNotFoundException;
import com.nba.attainment.exception.CalculationException;
import com.nba.attainment.exception.InvalidAttainmentException;
import com.nba.attainment.provider.*;
import com.nba.attainment.repository.POAttainmentRepository;
import com.nba.attainment.repository.PSOAttainmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * Application service (orchestrator) for PO/PSO attainment.
 *
 * <h2>This class only orchestrates — it does NOT:</h2>
 * <ul>
 *   <li>Contain mathematical formulas (delegated to calculators/strategies)</li>
 *   <li>Access other modules' JPA repositories directly</li>
 *   <li>Build HTTP responses (delegated to controllers)</li>
 *   <li>Know about Spring MVC</li>
 * </ul>
 *
 * <h2>Orchestration flow for PO calculation:</h2>
 * <ol>
 *   <li>Validate request parameters</li>
 *   <li>Ask providers for: CO attainments, CO→PO mappings, PO definitions</li>
 *   <li>Resolve the requested calculation strategy</li>
 *   <li>Invoke {@link POAttainmentCalculator}</li>
 *   <li>Upsert results to the repository (idempotent)</li>
 *   <li>Return response DTOs</li>
 * </ol>
 */
@Service
@Transactional
public class POPSOAttainmentApplicationService {

    private static final Logger log =
            LoggerFactory.getLogger(POPSOAttainmentApplicationService.class);

    // ── Dependencies: providers ───────────────────────────────────────────────
    private final COAttainmentProvider           coAttainmentProvider;
    private final MappingProvider                mappingProvider;
    private final ProgramOutcomeProvider         programOutcomeProvider;
    private final ProgramSpecificOutcomeProvider programSpecificOutcomeProvider;

    // ── Dependencies: calculators ─────────────────────────────────────────────
    private final AttainmentStrategyRegistry strategyRegistry;

    // ── Dependencies: repositories ────────────────────────────────────────────
    private final POAttainmentRepository  poRepository;
    private final PSOAttainmentRepository psoRepository;

    // ── Dependencies: supporting ──────────────────────────────────────────────
    private final AttainmentMapper     mapper;
    private final AttainmentProperties properties;

    public POPSOAttainmentApplicationService(
            COAttainmentProvider coAttainmentProvider,
            MappingProvider mappingProvider,
            ProgramOutcomeProvider programOutcomeProvider,
            ProgramSpecificOutcomeProvider programSpecificOutcomeProvider,
            AttainmentStrategyRegistry strategyRegistry,
            POAttainmentRepository poRepository,
            PSOAttainmentRepository psoRepository,
            AttainmentMapper mapper,
            AttainmentProperties properties) {

        this.coAttainmentProvider           = coAttainmentProvider;
        this.mappingProvider                = mappingProvider;
        this.programOutcomeProvider         = programOutcomeProvider;
        this.programSpecificOutcomeProvider = programSpecificOutcomeProvider;
        this.strategyRegistry               = strategyRegistry;
        this.poRepository                   = poRepository;
        this.psoRepository                  = psoRepository;
        this.mapper                         = mapper;
        this.properties                     = properties;
    }

    // =========================================================================
    // PO Attainment Calculation
    // =========================================================================

    /**
     * Calculates and persists PO attainment for the given program and year.
     * Idempotent — calling twice with the same version updates existing records.
     */
    public List<POAttainmentResponse> calculatePOAttainment(POAttainmentCalculationRequest request) {

        log.info("PO attainment calculation started: program={}, year={}, method={}, version={}",
                 request.programId(), request.academicYear(),
                 request.resolvedMethod(), request.resolvedVersion());

        // 1. Fetch input data from providers
        List<ProgramOutcomeData> programOutcomes =
                programOutcomeProvider.getProgramOutcomes(request.programId());
        if (programOutcomes.isEmpty()) {
            throw new InvalidAttainmentException(
                    "No Program Outcomes found for programId=" + request.programId());
        }

        List<COAttainmentData> coAttainments =
                coAttainmentProvider.getAttainments(request.programId(), request.academicYear());
        if (coAttainments.isEmpty()) {
            throw new InvalidAttainmentException(
                    "No CO attainments found for programId=" + request.programId()
                    + ", academicYear=" + request.academicYear());
        }

        List<COMappingData> coPoMappings =
                mappingProvider.getCOtoPOMappings(request.programId());

        // 2. Resolve strategy and build a per-request calculator
        AttainmentCalculationStrategy strategy = strategyRegistry.resolve(request.resolvedMethod());
        POAttainmentCalculator calculator = new POAttainmentCalculator(strategy);

        // 3. Calculate
        List<AttainmentCalculationResult> results = calculator.calculateAll(
                programOutcomes, coAttainments, coPoMappings,
                request.programId(), request.academicYear(), request.resolvedVersion());

        // 4. Upsert results
        Map<UUID, ProgramOutcomeData> poLookup = toLookup(programOutcomes, ProgramOutcomeData::poId);

        List<POAttainmentResponse> responses = new ArrayList<>();
        for (AttainmentCalculationResult result : results) {
            POAttainment entity = upsertPOAttainment(result, request.resolvedMethod(),
                                                     request.resolvedVersion(), poLookup);
            responses.add(mapper.toPOResponse(entity, poLookup));
        }

        log.info("PO attainment calculation completed: {} POs persisted for program={} year={}",
                 responses.size(), request.programId(), request.academicYear());
        return responses;
    }

    // =========================================================================
    // PSO Attainment Calculation
    // =========================================================================

    public List<PSOAttainmentResponse> calculatePSOAttainment(PSOAttainmentCalculationRequest request) {

        log.info("PSO attainment calculation started: program={}, year={}, method={}, version={}",
                 request.programId(), request.academicYear(),
                 request.resolvedMethod(), request.resolvedVersion());

        List<ProgramSpecificOutcomeData> programSpecificOutcomes =
                programSpecificOutcomeProvider.getProgramSpecificOutcomes(request.programId());
        if (programSpecificOutcomes.isEmpty()) {
            throw new InvalidAttainmentException(
                    "No Program Specific Outcomes found for programId=" + request.programId());
        }

        List<COAttainmentData> coAttainments =
                coAttainmentProvider.getAttainments(request.programId(), request.academicYear());
        if (coAttainments.isEmpty()) {
            throw new InvalidAttainmentException(
                    "No CO attainments found for programId=" + request.programId()
                    + ", academicYear=" + request.academicYear());
        }

        List<COMappingData> coPsoMappings =
                mappingProvider.getCOtoPSOMappings(request.programId());

        AttainmentCalculationStrategy strategy = strategyRegistry.resolve(request.resolvedMethod());
        PSOAttainmentCalculator calculator = new PSOAttainmentCalculator(strategy);

        List<AttainmentCalculationResult> results = calculator.calculateAll(
                programSpecificOutcomes, coAttainments, coPsoMappings,
                request.programId(), request.academicYear(), request.resolvedVersion());

        Map<UUID, ProgramSpecificOutcomeData> psoLookup =
                toLookup(programSpecificOutcomes, ProgramSpecificOutcomeData::psoId);

        List<PSOAttainmentResponse> responses = new ArrayList<>();
        for (AttainmentCalculationResult result : results) {
            PSOAttainment entity = upsertPSOAttainment(result, request.resolvedMethod(),
                                                       request.resolvedVersion(), psoLookup);
            responses.add(mapper.toPSOResponse(entity, psoLookup));
        }

        log.info("PSO attainment calculation completed: {} PSOs persisted for program={} year={}",
                 responses.size(), request.programId(), request.academicYear());
        return responses;
    }

    // =========================================================================
    // Combined
    // =========================================================================

    public CombinedAttainmentResponse calculateAll(CombinedAttainmentCalculationRequest request) {
        log.info("Combined PO+PSO calculation: program={}, year={}",
                 request.programId(), request.academicYear());
        List<POAttainmentResponse>  poResults  = calculatePOAttainment(request.toPORequest());
        List<PSOAttainmentResponse> psoResults = calculatePSOAttainment(request.toPSORequest());
        return new CombinedAttainmentResponse(poResults, psoResults);
    }

    // =========================================================================
    // Queries — read-only
    // =========================================================================

    @Transactional(readOnly = true)
    public List<POAttainmentResponse> getPOAttainments(UUID programId, String academicYear) {
        List<POAttainment> entities = (academicYear != null && !academicYear.isBlank())
                ? poRepository.findByProgramIdAndAcademicYear(programId, academicYear)
                : poRepository.findByProgramId(programId);
        Map<UUID, ProgramOutcomeData> poLookup = buildPOLookup(programId);
        return entities.stream().map(e -> mapper.toPOResponse(e, poLookup)).toList();
    }

    @Transactional(readOnly = true)
    public List<PSOAttainmentResponse> getPSOAttainments(UUID programId, String academicYear) {
        List<PSOAttainment> entities = (academicYear != null && !academicYear.isBlank())
                ? psoRepository.findByProgramIdAndAcademicYear(programId, academicYear)
                : psoRepository.findByProgramId(programId);
        Map<UUID, ProgramSpecificOutcomeData> psoLookup = buildPSOLookup(programId);
        return entities.stream().map(e -> mapper.toPSOResponse(e, psoLookup)).toList();
    }

    @Transactional(readOnly = true)
    public POAttainmentResponse getPOAttainmentById(UUID id) {
        POAttainment entity = poRepository.findById(id)
                .orElseThrow(() -> new AttainmentNotFoundException("PO attainment not found: id=" + id));
        return mapper.toPOResponse(entity, buildPOLookup(entity.getProgramId()));
    }

    @Transactional(readOnly = true)
    public PSOAttainmentResponse getPSOAttainmentById(UUID id) {
        PSOAttainment entity = psoRepository.findById(id)
                .orElseThrow(() -> new AttainmentNotFoundException("PSO attainment not found: id=" + id));
        return mapper.toPSOResponse(entity, buildPSOLookup(entity.getProgramId()));
    }

    @Transactional(readOnly = true)
    public POAttainmentTraceResponse getPOTrace(UUID id) {
        POAttainment entity = poRepository.findById(id)
                .orElseThrow(() -> new AttainmentNotFoundException("PO attainment not found: id=" + id));
        return mapper.toPOTraceResponse(entity, buildPOLookup(entity.getProgramId()));
    }

    @Transactional(readOnly = true)
    public PSOAttainmentTraceResponse getPSOTrace(UUID id) {
        PSOAttainment entity = psoRepository.findById(id)
                .orElseThrow(() -> new AttainmentNotFoundException("PSO attainment not found: id=" + id));
        return mapper.toPSOTraceResponse(entity, buildPSOLookup(entity.getProgramId()));
    }

    @Transactional(readOnly = true)
    public AttainmentSummaryResponse getSummary(UUID programId, String academicYear) {
        List<POAttainmentResponse>  pos  = getPOAttainments(programId, academicYear);
        List<PSOAttainmentResponse> psos = getPSOAttainments(programId, academicYear);
        return mapper.toSummary(pos, psos);
    }

    // =========================================================================
    // Private helpers — upsert logic
    // =========================================================================

    private POAttainment upsertPOAttainment(
            AttainmentCalculationResult result,
            String method, String version,
            Map<UUID, ProgramOutcomeData> poLookup) {

        String traceJson = mapper.serializeTrace(result);
        ProgramOutcomeData po = poLookup.get(result.outcomeId());
        String poCode = po != null ? po.code() : result.outcomeCode();

        poRepository.upsert(
                result.programId(),
                result.outcomeId(),
                poCode,
                result.academicYear(),
                result.directAttainment(),
                result.indirectAttainment(),
                result.finalAttainment(),
                method,
                version,
                traceJson);

        return poRepository
                .findByProgramIdAndPoIdAndAcademicYearAndCalculationVersion(
                        result.programId(), result.outcomeId(),
                        result.academicYear(), version)
                .orElseThrow(() -> new CalculationException(
                        "Failed to retrieve saved PO attainment for " + poCode));
    }

    private PSOAttainment upsertPSOAttainment(
            AttainmentCalculationResult result,
            String method, String version,
            Map<UUID, ProgramSpecificOutcomeData> psoLookup) {

        String traceJson = mapper.serializeTrace(result);
        ProgramSpecificOutcomeData pso = psoLookup.get(result.outcomeId());
        String psoCode = pso != null ? pso.code() : result.outcomeCode();

        psoRepository.upsert(
                result.programId(),
                result.outcomeId(),
                psoCode,
                result.academicYear(),
                result.directAttainment(),
                result.indirectAttainment(),
                result.finalAttainment(),
                method,
                version,
                traceJson);

        return psoRepository
                .findByProgramIdAndPsoIdAndAcademicYearAndCalculationVersion(
                        result.programId(), result.outcomeId(),
                        result.academicYear(), version)
                .orElseThrow(() -> new CalculationException(
                        "Failed to retrieve saved PSO attainment for " + psoCode));
    }

    // =========================================================================
    // Private helpers — lookup builders
    // =========================================================================

    private Map<UUID, ProgramOutcomeData> buildPOLookup(UUID programId) {
        return toLookup(programOutcomeProvider.getProgramOutcomes(programId),
                        ProgramOutcomeData::poId);
    }

    private Map<UUID, ProgramSpecificOutcomeData> buildPSOLookup(UUID programId) {
        return toLookup(programSpecificOutcomeProvider.getProgramSpecificOutcomes(programId),
                        ProgramSpecificOutcomeData::psoId);
    }

    private <T> Map<UUID, T> toLookup(List<T> items, Function<T, UUID> keyExtractor) {
        return items.stream().collect(Collectors.toMap(keyExtractor, Function.identity()));
    }
}
