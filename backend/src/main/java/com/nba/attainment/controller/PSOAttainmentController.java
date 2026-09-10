package com.nba.attainment.controller;

import com.nba.attainment.dto.request.PSOAttainmentCalculationRequest;
import com.nba.attainment.dto.response.PSOAttainmentResponse;
import com.nba.attainment.dto.response.PSOAttainmentTraceResponse;
import com.nba.attainment.service.POPSOAttainmentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for PSO attainment operations.
 *
 * <p>HTTP binding only — no business logic, no calculations.
 */
@RestController
@RequestMapping("/api/v1/attainment/pso")
@Tag(name = "PSO Attainment", description = "Program Specific Outcome attainment calculation and retrieval")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class PSOAttainmentController {

    private final POPSOAttainmentApplicationService service;

    public PSOAttainmentController(POPSOAttainmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate PSO attainment",
        description = "Triggers PSO attainment calculation for the specified program and academic year."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calculation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "422", description = "Business validation failed",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<List<PSOAttainmentResponse>> calculate(
            @Valid @RequestBody PSOAttainmentCalculationRequest request) {
        return ResponseEntity.ok(service.calculatePSOAttainment(request));
    }

    @GetMapping
    @Operation(
        summary = "Get all PSO attainments",
        description = "Returns all persisted PSO attainment records for a program. Filter by academicYear."
    )
    public ResponseEntity<List<PSOAttainmentResponse>> getAll(
            @Parameter(description = "Filter by program UUID", required = true)
            @RequestParam UUID programId,
            @Parameter(description = "Filter by academic year, e.g. 2023-24")
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(service.getPSOAttainments(programId, academicYear));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PSO attainment by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record found"),
        @ApiResponse(responseCode = "404", description = "Record not found",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<PSOAttainmentResponse> getById(
            @Parameter(description = "PSO attainment record UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(service.getPSOAttainmentById(id));
    }

    @GetMapping("/{id}/trace")
    @Operation(
        summary = "Get PSO attainment calculation trace",
        description = "Returns the full drill-down trace for a PSO attainment record."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trace found"),
        @ApiResponse(responseCode = "404", description = "Record not found",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<PSOAttainmentTraceResponse> getTrace(
            @Parameter(description = "PSO attainment record UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(service.getPSOTrace(id));
    }
}
