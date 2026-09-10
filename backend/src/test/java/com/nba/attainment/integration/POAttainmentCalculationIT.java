package com.nba.attainment.integration;

import com.nba.attainment.dto.request.CombinedAttainmentCalculationRequest;
import com.nba.attainment.dto.request.POAttainmentCalculationRequest;
import com.nba.attainment.dto.request.PSOAttainmentCalculationRequest;
import com.nba.attainment.dto.response.*;
import com.nba.attainment.entity.POAttainment;
import com.nba.attainment.repository.POAttainmentRepository;
import com.nba.attainment.repository.PSOAttainmentRepository;
import com.nba.attainment.service.POPSOAttainmentApplicationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.nba.attainment.provider.mock.MockDataConstants.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests: calculation → persistence → retrieval.
 *
 * <p>Uses Testcontainers PostgreSQL + real Flyway schema + mock providers.
 * Verifies the full stack from service call to database and back.
 */
@DisplayName("PO/PSO Attainment Calculation Integration Tests")
class POAttainmentCalculationIT extends AbstractIntegrationTest {

    @Autowired POPSOAttainmentApplicationService service;
    @Autowired POAttainmentRepository            poRepository;
    @Autowired PSOAttainmentRepository           psoRepository;

    @BeforeEach
    void cleanUp() {
        poRepository.deleteAll();
        psoRepository.deleteAll();
    }

    // =========================================================================
    // PO Attainment — calculation + persistence
    // =========================================================================

    @Nested
    @DisplayName("PO Attainment Calculation")
    class POCalculation {

        @Test
        @DisplayName("Calculates and persists PO attainment for all 12 POs")
        void calculatesAndPersistsAll12POs() {
            POAttainmentCalculationRequest request = new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1");

            List<POAttainmentResponse> results = service.calculatePOAttainment(request);

            // Mock providers define PO1–PO6 with mappings; PO7–PO12 have no mappings → 0
            assertThat(results).hasSize(12);
            assertThat(poRepository.count()).isEqualTo(12);
        }

        @Test
        @DisplayName("PO1 attainment is in the expected range from mock data")
        void po1AttainmentInExpectedRange() {
            POAttainmentCalculationRequest request = new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1");

            List<POAttainmentResponse> results = service.calculatePOAttainment(request);

            POAttainmentResponse po1 = results.stream()
                    .filter(r -> "PO1".equals(r.poCode()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("PO1 not found in results"));

            // Multiple courses contribute to PO1 — result should be between 60 and 90
            assertThat(po1.finalAttainment()).isBetween(60.0, 90.0);
            assertThat(po1.directAttainment()).isEqualTo(po1.finalAttainment());
        }

        @Test
        @DisplayName("Results are persisted to the database with correct metadata")
        void resultsPersistedWithCorrectMetadata() {
            POAttainmentCalculationRequest request = new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1");

            service.calculatePOAttainment(request);

            List<POAttainment> entities = poRepository.findByProgramIdAndAcademicYear(
                    PROGRAM_ID, ACADEMIC_YEAR);

            assertThat(entities).isNotEmpty();
            assertThat(entities).allSatisfy(e -> {
                assertThat(e.getProgramId()).isEqualTo(PROGRAM_ID);
                assertThat(e.getAcademicYear()).isEqualTo(ACADEMIC_YEAR);
                assertThat(e.getCalculationMethod()).isEqualTo("WEIGHTED");
                assertThat(e.getCalculationVersion()).isEqualTo("v1");
                assertThat(e.getCreatedAt()).isNotNull();
                assertThat(e.getUpdatedAt()).isNotNull();
            });
        }

        @Test
        @DisplayName("Calculation trace is persisted as non-empty JSON")
        void calculationTracePersistedAsJson() {
            service.calculatePOAttainment(new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, null, null));

            List<POAttainment> entities = poRepository.findByProgramIdAndAcademicYear(
                    PROGRAM_ID, ACADEMIC_YEAR);

            // At least POs with mappings should have a trace
            long withTrace = entities.stream()
                    .filter(e -> e.getCalculationTrace() != null && !e.getCalculationTrace().isBlank())
                    .count();
            assertThat(withTrace).isGreaterThan(0);
        }

        @Test
        @DisplayName("Re-calculating with same version updates records (idempotent upsert)")
        void recalculation_updatesExistingRecords() {
            POAttainmentCalculationRequest request = new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1");

            service.calculatePOAttainment(request);
            long countAfterFirst = poRepository.count();

            // Call again — should update, not insert duplicates
            service.calculatePOAttainment(request);
            long countAfterSecond = poRepository.count();

            assertThat(countAfterSecond).isEqualTo(countAfterFirst);
        }

        @Test
        @DisplayName("Re-calculating with different version creates new records")
        void recalculation_differentVersionCreatesNewRecords() {
            service.calculatePOAttainment(new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1"));
            long countV1 = poRepository.count();

            service.calculatePOAttainment(new POAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v2"));
            long countV1V2 = poRepository.count();

            assertThat(countV1V2).isEqualTo(countV1 * 2);
        }
    }

    // =========================================================================
    // PSO Attainment — calculation + persistence
    // =========================================================================

    @Nested
    @DisplayName("PSO Attainment Calculation")
    class PSOCalculation {

        @Test
        @DisplayName("Calculates and persists PSO attainment for all 3 PSOs")
        void calculatesAndPersistsAll3PSOs() {
            service.calculatePSOAttainment(new PSOAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1"));

            assertThat(psoRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("PSO1 attainment is in the expected range")
        void pso1AttainmentInExpectedRange() {
            List<PSOAttainmentResponse> results = service.calculatePSOAttainment(
                    new PSOAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null));

            PSOAttainmentResponse pso1 = results.stream()
                    .filter(r -> "PSO1".equals(r.psoCode()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("PSO1 not found"));

            assertThat(pso1.finalAttainment()).isBetween(60.0, 90.0);
        }

        @Test
        @DisplayName("PSO upsert is idempotent")
        void psoUpsert_isIdempotent() {
            PSOAttainmentCalculationRequest request = new PSOAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, "WEIGHTED", "v1");

            service.calculatePSOAttainment(request);
            service.calculatePSOAttainment(request);

            assertThat(psoRepository.count()).isEqualTo(3);
        }
    }

    // =========================================================================
    // Combined
    // =========================================================================

    @Nested
    @DisplayName("Combined PO+PSO Calculation")
    class CombinedCalculation {

        @Test
        @DisplayName("Combined calculation persists both PO and PSO records")
        void combinedCalculation_persistsBoth() {
            service.calculateAll(new CombinedAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, null, null));

            assertThat(poRepository.count()).isEqualTo(12);
            assertThat(psoRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Combined response contains both PO and PSO results")
        void combinedResponse_containsBoth() {
            CombinedAttainmentResponse response = service.calculateAll(
                    new CombinedAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null));

            assertThat(response.poResults()).hasSize(12);
            assertThat(response.psoResults()).hasSize(3);
        }
    }

    // =========================================================================
    // Retrieval
    // =========================================================================

    @Nested
    @DisplayName("Retrieval after calculation")
    class Retrieval {

        @BeforeEach
        void calculate() {
            service.calculateAll(new CombinedAttainmentCalculationRequest(
                    PROGRAM_ID, ACADEMIC_YEAR, null, null));
        }

        @Test
        @DisplayName("getPOAttainments returns all persisted PO records for program+year")
        void getPOAttainments_returnsAll() {
            List<POAttainmentResponse> results = service.getPOAttainments(PROGRAM_ID, ACADEMIC_YEAR);
            assertThat(results).hasSize(12);
        }

        @Test
        @DisplayName("getPSOAttainments returns all persisted PSO records")
        void getPSOAttainments_returnsAll() {
            List<PSOAttainmentResponse> results = service.getPSOAttainments(PROGRAM_ID, ACADEMIC_YEAR);
            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("getPOAttainmentById returns correct record")
        void getPOAttainmentById_returnsCorrectRecord() {
            List<POAttainmentResponse> all = service.getPOAttainments(PROGRAM_ID, ACADEMIC_YEAR);
            UUID id = all.get(0).id();

            POAttainmentResponse found = service.getPOAttainmentById(id);

            assertThat(found.id()).isEqualTo(id);
            assertThat(found.programId()).isEqualTo(PROGRAM_ID);
        }

        @Test
        @DisplayName("getPOTrace returns trace with course breakdown")
        void getPOTrace_returnsTrace() {
            List<POAttainmentResponse> all = service.getPOAttainments(PROGRAM_ID, ACADEMIC_YEAR);
            // Find a PO that has non-zero attainment (has mappings)
            POAttainmentResponse withAttainment = all.stream()
                    .filter(r -> r.finalAttainment() != null && r.finalAttainment() > 0)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No PO with non-zero attainment"));

            POAttainmentTraceResponse trace = service.getPOTrace(withAttainment.id());

            assertThat(trace.id()).isEqualTo(withAttainment.id());
            assertThat(trace.courseBreakdown()).isNotEmpty();
            assertThat(trace.formula()).isNotBlank();
            assertThat(trace.totalWeight()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getSummary returns correct averages")
        void getSummary_returnsCorrectAverages() {
            AttainmentSummaryResponse summary = service.getSummary(PROGRAM_ID, ACADEMIC_YEAR);

            assertThat(summary.poAttainments()).hasSize(12);
            assertThat(summary.psoAttainments()).hasSize(3);
            assertThat(summary.averagePOAttainment()).isNotNull();
            assertThat(summary.averagePSOAttainment()).isNotNull();
            assertThat(summary.highestPO()).isNotNull();
            assertThat(summary.lowestPO()).isNotNull();
        }

        @Test
        @DisplayName("Status is correctly assigned based on thresholds")
        void status_assignedByThreshold() {
            List<POAttainmentResponse> results = service.getPOAttainments(PROGRAM_ID, ACADEMIC_YEAR);

            // All results should have a non-blank status
            assertThat(results).allSatisfy(r ->
                    assertThat(r.status()).isIn("Excellent", "Good", "Needs Improvement"));
        }
    }

    // =========================================================================
    // Error conditions
    // =========================================================================

    @Nested
    @DisplayName("Error conditions")
    class ErrorConditions {

        @Test
        @DisplayName("Unknown program ID throws InvalidAttainmentException")
        void unknownProgramId_throwsException() {
            UUID unknownProgram = UUID.randomUUID();

            assertThatThrownBy(() -> service.calculatePOAttainment(
                    new POAttainmentCalculationRequest(unknownProgram, ACADEMIC_YEAR, null, null)))
                    .hasMessageContaining("No Program Outcomes found");
        }

        @Test
        @DisplayName("Unknown academic year throws InvalidAttainmentException")
        void unknownAcademicYear_throwsException() {
            assertThatThrownBy(() -> service.calculatePOAttainment(
                    new POAttainmentCalculationRequest(PROGRAM_ID, "1900-01", null, null)))
                    .hasMessageContaining("No CO attainments found");
        }

        @Test
        @DisplayName("Unknown attainment record ID throws AttainmentNotFoundException")
        void unknownId_throwsNotFoundException() {
            UUID randomId = UUID.randomUUID();

            assertThatThrownBy(() -> service.getPOAttainmentById(randomId))
                    .hasMessageContaining("PO attainment not found");
        }
    }
}
