package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.domain.COAttainmentData;
import com.nba.attainment.dto.domain.COMappingData;
import com.nba.attainment.dto.domain.ProgramOutcomeData;
import com.nba.attainment.exception.InvalidAttainmentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link POAttainmentCalculator}.
 *
 * <p>No Spring context, no database — plain Java.
 */
class POAttainmentCalculatorTest {

    private static final UUID PROGRAM_ID  = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID PO1_ID      = UUID.randomUUID();
    private static final UUID PO2_ID      = UUID.randomUUID();
    private static final UUID PO3_ID      = UUID.randomUUID();
    private static final UUID COURSE_DS   = UUID.fromString("22222222-0000-0000-0000-000000000001");
    private static final UUID DS_CO1      = UUID.randomUUID();
    private static final UUID DS_CO2      = UUID.randomUUID();
    private static final UUID DS_CO3      = UUID.randomUUID();

    private POAttainmentCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new POAttainmentCalculator(new WeightedCOAttainmentStrategy());
    }

    // ── Spec reference data ──────────────────────────────────────────────────

    private List<ProgramOutcomeData> threePOs() {
        return List.of(
                new ProgramOutcomeData(PO1_ID, PROGRAM_ID, "PO1", "Engineering Knowledge"),
                new ProgramOutcomeData(PO2_ID, PROGRAM_ID, "PO2", "Problem Analysis"),
                new ProgramOutcomeData(PO3_ID, PROGRAM_ID, "PO3", "Design Solutions")
        );
    }

    private List<COAttainmentData> dsAttainments() {
        return List.of(
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO1, "CO1", "2023-24", 75.0),
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO2, "CO2", "2023-24", 68.0),
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO3, "CO3", "2023-24", 82.0)
        );
    }

    private List<COMappingData> dsMappings() {
        return List.of(
                COMappingData.toPO(DS_CO1, "CO1", COURSE_DS, PO1_ID, 3),
                COMappingData.toPO(DS_CO2, "CO2", COURSE_DS, PO1_ID, 2),
                COMappingData.toPO(DS_CO3, "CO3", COURSE_DS, PO1_ID, 1),

                COMappingData.toPO(DS_CO1, "CO1", COURSE_DS, PO2_ID, 2),
                COMappingData.toPO(DS_CO2, "CO2", COURSE_DS, PO2_ID, 3),
                COMappingData.toPO(DS_CO3, "CO3", COURSE_DS, PO2_ID, 2),

                COMappingData.toPO(DS_CO1, "CO1", COURSE_DS, PO3_ID, 1),
                COMappingData.toPO(DS_CO2, "CO2", COURSE_DS, PO3_ID, 2),
                COMappingData.toPO(DS_CO3, "CO3", COURSE_DS, PO3_ID, 3)
        );
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Happy path calculations")
    class HappyPath {

        @Test
        @DisplayName("Returns one result per PO")
        void returnsOneResultPerPO() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("PO1 = 73.83% (spec reference)")
        void po1_specReference() {
            // (75×3 + 68×2 + 82×1) / (3+2+1) = 443/6 = 73.83
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            AttainmentCalculationResult po1 = results.stream()
                    .filter(r -> r.outcomeCode().equals("PO1"))
                    .findFirst().orElseThrow();

            assertThat(po1.directAttainment()).isEqualTo(73.83);
        }

        @Test
        @DisplayName("PO2 calculation — CO2 heavily weighted (mapping=3)")
        void po2_heavierCO2Weight() {
            // PO2: CO1=75 m=2, CO2=68 m=3, CO3=82 m=2
            // (150 + 204 + 164) / 7 = 518/7 = 74.0
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            AttainmentCalculationResult po2 = results.stream()
                    .filter(r -> r.outcomeCode().equals("PO2"))
                    .findFirst().orElseThrow();

            assertThat(po2.directAttainment()).isEqualTo(74.0);
        }

        @Test
        @DisplayName("Result outcome IDs match the PO IDs provided")
        void resultOutcomeIdsMatchPOs() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).extracting(AttainmentCalculationResult::outcomeId)
                    .containsExactlyInAnyOrder(PO1_ID, PO2_ID, PO3_ID);
        }

        @Test
        @DisplayName("Results carry correct programId and academicYear")
        void resultsCarryMetadata() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).allSatisfy(r -> {
                assertThat(r.programId()).isEqualTo(PROGRAM_ID);
                assertThat(r.academicYear()).isEqualTo("2023-24");
            });
        }
    }

    // -------------------------------------------------------------------------
    // Missing data handling
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Missing data handling")
    class MissingData {

        @Test
        @DisplayName("PO with no mappings → attainment = 0")
        void poWithNoMappings_attainmentIsZero() {
            // PO4 has no mappings in dsMappings
            UUID po4Id = UUID.randomUUID();
            List<ProgramOutcomeData> posWithExtra = List.of(
                    new ProgramOutcomeData(po4Id, PROGRAM_ID, "PO4", "Research"));

            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    posWithExtra, dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results.get(0).directAttainment()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("CO in mapping without attainment is skipped gracefully")
        void coWithNoAttainment_isSkipped() {
            UUID unknownCo = UUID.randomUUID();
            List<COMappingData> mappingsWithUnknown = List.of(
                    COMappingData.toPO(DS_CO1, "CO1", COURSE_DS, PO1_ID, 3),
                    // This CO has no attainment in dsAttainments()
                    COMappingData.toPO(unknownCo, "CO_UNKNOWN", COURSE_DS, PO1_ID, 2)
            );

            // Should not throw — should use only CO1's contribution
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    List.of(new ProgramOutcomeData(PO1_ID, PROGRAM_ID, "PO1", "Test")),
                    dsAttainments(), mappingsWithUnknown,
                    PROGRAM_ID, "2023-24", "v1");

            // Only CO1 contributes: 75×3/3 = 75.0
            assertThat(results.get(0).directAttainment()).isEqualTo(75.0);
        }

        @Test
        @DisplayName("Empty PO list returns empty results")
        void emptyPOList_returnsEmpty() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    List.of(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation")
    class Validation {

        @Test
        @DisplayName("Null programId throws InvalidAttainmentException")
        void nullProgramId_throws() {
            assertThatThrownBy(() -> calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    null, "2023-24", "v1"))
                    .isInstanceOf(InvalidAttainmentException.class);
        }

        @Test
        @DisplayName("Blank academicYear throws InvalidAttainmentException")
        void blankAcademicYear_throws() {
            assertThatThrownBy(() -> calculator.calculateAll(
                    threePOs(), dsAttainments(), dsMappings(),
                    PROGRAM_ID, "  ", "v1"))
                    .isInstanceOf(InvalidAttainmentException.class);
        }

        @Test
        @DisplayName("CO attainment with wrong programId throws InvalidAttainmentException")
        void wrongProgramId_throws() {
            UUID wrongProgram = UUID.randomUUID();
            List<COAttainmentData> badAttainments = List.of(
                    new COAttainmentData(wrongProgram, COURSE_DS, "DS", DS_CO1, "CO1", "2023-24", 75.0)
            );

            assertThatThrownBy(() -> calculator.calculateAll(
                    threePOs(), badAttainments, dsMappings(),
                    PROGRAM_ID, "2023-24", "v1"))
                    .isInstanceOf(InvalidAttainmentException.class)
                    .hasMessageContaining("programId mismatch");
        }
    }

    // -------------------------------------------------------------------------
    // Multiple courses
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("COs from two courses both contribute to the same PO")
    void multipleCourses_contributeToSamePO() {
        UUID courseOOP   = UUID.fromString("22222222-0000-0000-0000-000000000002");
        UUID oopCo1      = UUID.randomUUID();

        List<COAttainmentData> allAttainments = List.of(
                new COAttainmentData(PROGRAM_ID, COURSE_DS,  "Data Structures", DS_CO1, "CO1", "2023-24", 75.0),
                new COAttainmentData(PROGRAM_ID, courseOOP, "OOP",              oopCo1, "CO1", "2023-24", 85.0)
        );

        List<COMappingData> allMappings = List.of(
                COMappingData.toPO(DS_CO1,  "CO1", COURSE_DS,  PO1_ID, 3),
                COMappingData.toPO(oopCo1, "CO1", courseOOP, PO1_ID, 3)
        );

        List<ProgramOutcomeData> pos = List.of(
                new ProgramOutcomeData(PO1_ID, PROGRAM_ID, "PO1", "Engineering Knowledge"));

        List<AttainmentCalculationResult> results = calculator.calculateAll(
                pos, allAttainments, allMappings,
                PROGRAM_ID, "2023-24", "v1");

        // (75×3 + 85×3) / (3+3) = (225+255)/6 = 480/6 = 80.0
        assertThat(results.get(0).directAttainment()).isEqualTo(80.0);

        // Trace should show 2 courses
        assertThat(results.get(0).trace().courseBreakdown()).hasSize(2);
    }
}
