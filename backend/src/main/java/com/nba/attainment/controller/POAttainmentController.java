package com.nba.attainment.controller;

import com.nba.attainment.dto.request.POAttainmentCalculationRequest;
import com.nba.attainment.dto.response.POAttainmentResponse;
import com.nba.attainment.dto.response.POAttainmentTraceResponse;
import com.nba.attainment.service.POPSOAttainmentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for PO attainment operations.
 *
 * <p>Responsibilities: HTTP binding, request validation, response mapping.
 * No business logic or calculation here — all delegated to the application service.
 */
@RestController
@RequestMapping("/api/v1/attainment/po")
@Tag(name = "PO Attainment", description = "Program Outcome attainment calculation and retrieval")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class POAttainmentController {

    private final POPSOAttainmentApplicationService service;

    public POAttainmentController(POPSOAttainmentApplicationService service) {
        this.service = service;
    }

    // ── Calculation ───────────────────────────────────────────────────────────

    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate PO attainment",
        description = "Triggers PO attainment calculation for the specified program and academic year. " +
                      "Results are persisted and returned. Calling again with the same version updates existing records."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calculation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "422", description = "Business validation failed — e.g. no COs found",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<List<POAttainmentResponse>> calculate(
            @Valid @RequestBody POAttainmentCalculationRequest request) {
        List<POAttainmentResponse> results = service.calculatePOAttainment(request);
        return ResponseEntity.ok(results);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all PO attainments",
        description = "Returns all persisted PO attainment records. Filter by programId and/or academicYear."
    )
    public ResponseEntity<List<POAttainmentResponse>> getAll(
            @Parameter(description = "Filter by program UUID", required = true)
            @RequestParam UUID programId,
            @Parameter(description = "Filter by academic year, e.g. 2023-24")
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(service.getPOAttainments(programId, academicYear));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PO attainment by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record found"),
        @ApiResponse(responseCode = "404", description = "Record not found",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<POAttainmentResponse> getById(
            @Parameter(description = "PO attainment record UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(service.getPOAttainmentById(id));
    }

    @GetMapping("/{id}/trace")
    @Operation(
        summary = "Get PO attainment calculation trace",
        description = "Returns the full drill-down trace: formula, course breakdown, CO contributions, weighted values."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trace found"),
        @ApiResponse(responseCode = "404", description = "Record not found",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<POAttainmentTraceResponse> getTrace(
            @Parameter(description = "PO attainment record UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(service.getPOTrace(id));
    }
}
