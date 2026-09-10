package com.nba.attainment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nba.attainment.config.AttainmentProperties;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult.CalculationTrace;
import com.nba.attainment.dto.domain.ProgramOutcomeData;
import com.nba.attainment.dto.domain.ProgramSpecificOutcomeData;
import com.nba.attainment.dto.response.*;
import com.nba.attainment.entity.POAttainment;
import com.nba.attainment.entity.PSOAttainment;
import com.nba.attainment.exception.CalculationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Maps between internal domain/calculation objects and API response DTOs.
 *
 * <p>Centralising this mapping keeps the application service and controllers
 * free of serialisation / transformation logic.
 */
@Component
public class AttainmentMapper {

    private final ObjectMapper objectMapper;
    private final AttainmentProperties properties;

    public AttainmentMapper(ObjectMapper objectMapper, AttainmentProperties properties) {
        this.objectMapper = objectMapper;
        this.properties   = properties;
    }

    // ── Entity → Response DTO ─────────────────────────────────────────────────

    public POAttainmentResponse toPOResponse(POAttainment entity,
                                              Map<UUID, ProgramOutcomeData> poLookup) {
        ProgramOutcomeData po = poLookup.get(entity.getPoId());
        String description = po != null ? po.description() : "";
        String status = properties.thresholds()
                .statusFor(entity.getFinalAttainment() != null ? entity.getFinalAttainment() : 0.0);

        return new POAttainmentResponse(
                entity.getId(),
                entity.getProgramId(),
                entity.getPoId(),
                entity.getPoCode(),
                description,
                entity.getAcademicYear(),
                entity.getDirectAttainment(),
                entity.getIndirectAttainment(),
                entity.getFinalAttainment(),
                entity.getCalculationMethod(),
                entity.getCalculationVersion(),
                status,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PSOAttainmentResponse toPSOResponse(PSOAttainment entity,
                                                Map<UUID, ProgramSpecificOutcomeData> psoLookup) {
        ProgramSpecificOutcomeData pso = psoLookup.get(entity.getPsoId());
        String description = pso != null ? pso.description() : "";
        String status = properties.thresholds()
                .statusFor(entity.getFinalAttainment() != null ? entity.getFinalAttainment() : 0.0);

        return new PSOAttainmentResponse(
                entity.getId(),
                entity.getProgramId(),
                entity.getPsoId(),
                entity.getPsoCode(),
                description,
                entity.getAcademicYear(),
                entity.getDirectAttainment(),
                entity.getIndirectAttainment(),
                entity.getFinalAttainment(),
                entity.getCalculationMethod(),
                entity.getCalculationVersion(),
                status,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // ── Trace response ────────────────────────────────────────────────────────

    public POAttainmentTraceResponse toPOTraceResponse(POAttainment entity,
                                                        Map<UUID, ProgramOutcomeData> poLookup) {
        ProgramOutcomeData po = poLookup.get(entity.getPoId());
        String description = po != null ? po.description() : "";

        CalculationTrace trace = parseTrace(entity.getCalculationTrace());

        List<POAttainmentTraceResponse.CourseBreakdown> courseBreakdown = trace != null
                ? trace.courseBreakdown().stream()
                        .map(cb -> new POAttainmentTraceResponse.CourseBreakdown(
                                cb.courseId(), cb.courseName(),
                                cb.coContributions().stream()
                                        .map(d -> new POAttainmentTraceResponse.CODetail(
                                                d.coId(), d.coCode(),
                                                d.attainment(), d.mappingLevel(), d.weightedValue()))
                                        .toList()))
                        .toList()
                : List.of();

        return new POAttainmentTraceResponse(
                entity.getId(),
                entity.getProgramId(),
                entity.getPoId(),
                entity.getPoCode(),
                description,
                entity.getAcademicYear(),
                entity.getDirectAttainment(),
                entity.getIndirectAttainment(),
                entity.getFinalAttainment(),
                entity.getCalculationMethod(),
                entity.getCalculationVersion(),
                trace != null ? trace.formula() : "",
                trace != null ? trace.totalWeight() : 0.0,
                trace != null ? trace.weightedSum() : 0.0,
                courseBreakdown,
                entity.getCreatedAt()
        );
    }

    public PSOAttainmentTraceResponse toPSOTraceResponse(PSOAttainment entity,
                                                          Map<UUID, ProgramSpecificOutcomeData> psoLookup) {
        ProgramSpecificOutcomeData pso = psoLookup.get(entity.getPsoId());
        String description = pso != null ? pso.description() : "";

        CalculationTrace trace = parseTrace(entity.getCalculationTrace());

        List<PSOAttainmentTraceResponse.CourseBreakdown> courseBreakdown = trace != null
                ? trace.courseBreakdown().stream()
                        .map(cb -> new PSOAttainmentTraceResponse.CourseBreakdown(
                                cb.courseId(), cb.courseName(),
                                cb.coContributions().stream()
                                        .map(d -> new PSOAttainmentTraceResponse.CODetail(
                                                d.coId(), d.coCode(),
                                                d.attainment(), d.mappingLevel(), d.weightedValue()))
                                        .toList()))
                        .toList()
                : List.of();

        return new PSOAttainmentTraceResponse(
                entity.getId(),
                entity.getProgramId(),
                entity.getPsoId(),
                entity.getPsoCode(),
                description,
                entity.getAcademicYear(),
                entity.getDirectAttainment(),
                entity.getIndirectAttainment(),
                entity.getFinalAttainment(),
                entity.getCalculationMethod(),
                entity.getCalculationVersion(),
                trace != null ? trace.formula() : "",
                trace != null ? trace.totalWeight() : 0.0,
                trace != null ? trace.weightedSum() : 0.0,
                courseBreakdown,
                entity.getCreatedAt()
        );
    }

    // ── Calculation result → Entity ───────────────────────────────────────────

    public String serializeTrace(AttainmentCalculationResult result) {
        try {
            return objectMapper.writeValueAsString(result.trace());
        } catch (JsonProcessingException e) {
            throw new CalculationException("Failed to serialise calculation trace: " + e.getMessage(), e);
        }
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    public AttainmentSummaryResponse toSummary(
            List<POAttainmentResponse> poResponses,
            List<PSOAttainmentResponse> psoResponses) {

        Double avgPO = average(poResponses.stream()
                .map(POAttainmentResponse::finalAttainment).toList());
        Double avgPSO = average(psoResponses.stream()
                .map(PSOAttainmentResponse::finalAttainment).toList());

        POAttainmentResponse highestPO = poResponses.stream()
                .filter(r -> r.finalAttainment() != null)
                .max(java.util.Comparator.comparingDouble(POAttainmentResponse::finalAttainment))
                .orElse(null);
        POAttainmentResponse lowestPO = poResponses.stream()
                .filter(r -> r.finalAttainment() != null)
                .min(java.util.Comparator.comparingDouble(POAttainmentResponse::finalAttainment))
                .orElse(null);
        PSOAttainmentResponse highestPSO = psoResponses.stream()
                .filter(r -> r.finalAttainment() != null)
                .max(java.util.Comparator.comparingDouble(PSOAttainmentResponse::finalAttainment))
                .orElse(null);
        PSOAttainmentResponse lowestPSO = psoResponses.stream()
                .filter(r -> r.finalAttainment() != null)
                .min(java.util.Comparator.comparingDouble(PSOAttainmentResponse::finalAttainment))
                .orElse(null);

        return new AttainmentSummaryResponse(
                poResponses, psoResponses, avgPO, avgPSO,
                highestPO, lowestPO, highestPSO, lowestPSO);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CalculationTrace parseTrace(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, CalculationTrace.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Double average(List<Double> values) {
        return values.stream()
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .boxed()
                .map(d -> Math.round(d * 100.0) / 100.0)
                .findFirst()
                .orElse(null);
    }
}
