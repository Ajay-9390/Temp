package com.nba.attainment.integration;

import com.nba.attainment.dto.request.CombinedAttainmentCalculationRequest;
import com.nba.attainment.dto.request.POAttainmentCalculationRequest;
import com.nba.attainment.repository.POAttainmentRepository;
import com.nba.attainment.repository.PSOAttainmentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nba.attainment.provider.mock.MockDataConstants.*;
import static org.assertj.core.api.Assertions.*;

/**
 * HTTP-level integration tests against the REST API.
 * Uses {@link TestRestTemplate} for real HTTP through the full MVC stack.
 */
@DisplayName("REST API Integration Tests")
@SuppressWarnings({"rawtypes", "unchecked"})
class AttainmentApiIT extends AbstractIntegrationTest {

    // Typed references so AssertJ can resolve key types correctly
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @Autowired TestRestTemplate        restTemplate;
    @Autowired POAttainmentRepository  poRepository;
    @Autowired PSOAttainmentRepository psoRepository;

    @BeforeEach
    void cleanUp() {
        poRepository.deleteAll();
        psoRepository.deleteAll();
    }

    // ── PO calculate endpoint ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/attainment/po/calculate returns 200 with 12 PO results")
    void postCalculatePO_returns200() {
        POAttainmentCalculationRequest body =
                new POAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/v1/attainment/po/calculate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                LIST_OF_MAPS);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(12);
    }

    @Test
    @DisplayName("POST /api/v1/attainment/po/calculate with missing programId returns 400")
    void postCalculatePO_missingProgramId_returns400() {
        String body = """
                {"academicYear":"2023-24"}
                """;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/po/calculate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    @DisplayName("POST /api/v1/attainment/po/calculate with invalid year format returns 400")
    void postCalculatePO_invalidYearFormat_returns400() {
        String body = """
                {"programId":"%s","academicYear":"not-a-year"}
                """.formatted(PROGRAM_ID);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/po/calculate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── PSO calculate endpoint ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/attainment/pso/calculate returns 200 with 3 PSO results")
    void postCalculatePSO_returns200() {
        String body = """
                {"programId":"%s","academicYear":"%s"}
                """.formatted(PROGRAM_ID, ACADEMIC_YEAR);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/v1/attainment/pso/calculate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                LIST_OF_MAPS);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
    }

    // ── Combined endpoint ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/attainment/calculate returns both PO and PSO results")
    void postCalculateAll_returnsBoth() {
        CombinedAttainmentCalculationRequest body =
                new CombinedAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/calculate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                MAP_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("poResults");
        assertThat(response.getBody()).containsKey("psoResults");
    }

    // ── Summary endpoint ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/attainment/summary returns summary after calculation")
    void getSummary_returnsData() {
        // Seed data
        restTemplate.exchange(
                "/api/v1/attainment/calculate",
                HttpMethod.POST,
                new HttpEntity<>(
                        new CombinedAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null),
                        jsonHeaders()),
                MAP_TYPE);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/summary?programId={pid}&academicYear={year}",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                MAP_TYPE,
                PROGRAM_ID, ACADEMIC_YEAR);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("poAttainments");
        assertThat(response.getBody()).containsKey("psoAttainments");
        assertThat(response.getBody()).containsKey("averagePOAttainment");
    }

    // ── GET by ID endpoints ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/attainment/po/{id} returns 404 for unknown ID")
    void getPOById_unknown_returns404() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/po/{id}",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                MAP_TYPE,
                UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "NOT_FOUND");
    }

    @Test
    @DisplayName("GET /api/v1/attainment/po/{id}/trace returns trace after calculation")
    void getPOTrace_returnsTrace() {
        // Calculate first
        ResponseEntity<List<Map<String, Object>>> calcResponse = restTemplate.exchange(
                "/api/v1/attainment/po/calculate",
                HttpMethod.POST,
                new HttpEntity<>(
                        new POAttainmentCalculationRequest(PROGRAM_ID, ACADEMIC_YEAR, null, null),
                        jsonHeaders()),
                LIST_OF_MAPS);

        assertThat(calcResponse.getBody()).isNotEmpty();
        String id = (String) calcResponse.getBody().get(0).get("id");

        ResponseEntity<Map<String, Object>> traceResponse = restTemplate.exchange(
                "/api/v1/attainment/po/{id}/trace",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                MAP_TYPE,
                id);

        assertThat(traceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(traceResponse.getBody()).containsKey("courseBreakdown");
        assertThat(traceResponse.getBody()).containsKey("formula");
    }

    // ── Error response structure ──────────────────────────────────────────────

    @Test
    @DisplayName("Error response has timestamp, status, error, message, path fields")
    void errorResponse_hasCorrectStructure() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/attainment/po/{id}",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                MAP_TYPE,
                UUID.randomUUID());

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("timestamp");
        assertThat(body).containsKey("status");
        assertThat(body).containsKey("error");
        assertThat(body).containsKey("message");
        assertThat(body).containsKey("path");
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("path").toString()).contains("/api/v1/attainment/po/");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
