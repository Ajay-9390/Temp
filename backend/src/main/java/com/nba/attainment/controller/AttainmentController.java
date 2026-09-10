package com.nba.attainment.controller;

import com.nba.attainment.dto.request.CombinedAttainmentCalculationRequest;
import com.nba.attainment.dto.response.AttainmentSummaryResponse;
import com.nba.attainment.dto.response.CombinedAttainmentResponse;
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

import java.util.UUID;

/**
 * REST controller for combined PO+PSO operations and the summary dashboard endpoint.
 */
@RestController
@RequestMapping("/api/v1/attainment")
@Tag(name = "Attainment", description = "Combined PO+PSO attainment and dashboard summary")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class AttainmentController {

    private final POPSOAttainmentApplicationService service;

    public AttainmentController(POPSOAttainmentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate both PO and PSO attainment",
        description = "Convenience endpoint that calculates and persists both PO and PSO attainment in one call."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calculation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "422", description = "Business validation failed",
                     content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<CombinedAttainmentResponse> calculateAll(
            @Valid @RequestBody CombinedAttainmentCalculationRequest request) {
        return ResponseEntity.ok(service.calculateAll(request));
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get attainment summary for dashboard",
        description = "Returns a summary of all PO and PSO attainments including averages, highest, and lowest values."
    )
    public ResponseEntity<AttainmentSummaryResponse> getSummary(
            @Parameter(description = "Program UUID", required = true)
            @RequestParam UUID programId,
            @Parameter(description = "Academic year filter, e.g. 2023-24")
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(service.getSummary(programId, academicYear));
    }
}
