package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.domain.COAttainmentData;
import com.nba.attainment.dto.domain.COMappingData;
import com.nba.attainment.dto.domain.ProgramSpecificOutcomeData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PSOAttainmentCalculator}.
 *
 * <p>No Spring context, no database — plain Java.
 */
class PSOAttainmentCalculatorTest {

    private static final UUID PROGRAM_ID = UUID.fromString("11111111-0000-0000-0000-000000000001");
    private static final UUID PSO1_ID    = UUID.randomUUID();
    private static final UUID PSO2_ID    = UUID.randomUUID();
    private static final UUID PSO3_ID    = UUID.randomUUID();
    private static final UUID COURSE_DS  = UUID.fromString("22222222-0000-0000-0000-000000000001");
    private static final UUID DS_CO1     = UUID.randomUUID();
    private static final UUID DS_CO2     = UUID.randomUUID();
    private static final UUID DS_CO3     = UUID.randomUUID();

    private PSOAttainmentCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PSOAttainmentCalculator(new WeightedCOAttainmentStrategy());
    }

    private List<ProgramSpecificOutcomeData> threePSOs() {
        return List.of(
                new ProgramSpecificOutcomeData(PSO1_ID, PROGRAM_ID, "PSO1", "Software Engineering"),
                new ProgramSpecificOutcomeData(PSO2_ID, PROGRAM_ID, "PSO2", "Algorithms & Data"),
                new ProgramSpecificOutcomeData(PSO3_ID, PROGRAM_ID, "PSO3", "Emerging Technologies")
        );
    }

    private List<COAttainmentData> dsAttainments() {
        return List.of(
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO1, "CO1", "2023-24", 75.0),
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO2, "CO2", "2023-24", 68.0),
                new COAttainmentData(PROGRAM_ID, COURSE_DS, "Data Structures", DS_CO3, "CO3", "2023-24", 82.0)
        );
    }

    private List<COMappingData> dsPSOMappings() {
        return List.of(
                // PSO1: CO1=m3, CO2=m2, CO3=m1
                COMappingData.toPSO(DS_CO1, "CO1", COURSE_DS, PSO1_ID, 3),
                COMappingData.toPSO(DS_CO2, "CO2", COURSE_DS, PSO1_ID, 2),
                COMappingData.toPSO(DS_CO3, "CO3", COURSE_DS, PSO1_ID, 1),

                // PSO2: CO1=m2, CO2=m3, CO3=m2
                COMappingData.toPSO(DS_CO1, "CO1", COURSE_DS, PSO2_ID, 2),
                COMappingData.toPSO(DS_CO2, "CO2", COURSE_DS, PSO2_ID, 3),
                COMappingData.toPSO(DS_CO3, "CO3", COURSE_DS, PSO2_ID, 2),

                // PSO3: CO1=m1, CO2=m2, CO3=m3
                COMappingData.toPSO(DS_CO1, "CO1", COURSE_DS, PSO3_ID, 1),
                COMappingData.toPSO(DS_CO2, "CO2", COURSE_DS, PSO3_ID, 2),
                COMappingData.toPSO(DS_CO3, "CO3", COURSE_DS, PSO3_ID, 3)
        );
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Returns one result per PSO")
        void returnsOneResultPerPSO() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePSOs(), dsAttainments(), dsPSOMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("PSO1 = 73.83% (same formula as PO1 with same data)")
        void pso1_matchesPO1FormulaWithSameData() {
            // PSO1: CO1=75×3, CO2=68×2, CO3=82×1 → 443/6 = 73.83
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePSOs(), dsAttainments(), dsPSOMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            AttainmentCalculationResult pso1 = results.stream()
                    .filter(r -> r.outcomeCode().equals("PSO1"))
                    .findFirst().orElseThrow();

            assertThat(pso1.directAttainment()).isEqualTo(73.83);
        }

        @Test
        @DisplayName("PSO3 has higher weight on CO3 (82%) → pulls attainment up")
        void pso3_higherCO3Weight() {
            // PSO3: CO1=75×1, CO2=68×2, CO3=82×3 = (75+136+246)/6 = 457/6 = 76.17
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePSOs(), dsAttainments(), dsPSOMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            AttainmentCalculationResult pso3 = results.stream()
                    .filter(r -> r.outcomeCode().equals("PSO3"))
                    .findFirst().orElseThrow();

            assertThat(pso3.directAttainment()).isEqualTo(76.17);
        }

        @Test
        @DisplayName("Strategy name in result matches WEIGHTED")
        void strategyNameInResult() {
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    threePSOs(), dsAttainments(), dsPSOMappings(),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results).allSatisfy(r ->
                    assertThat(r.strategyName()).isEqualTo("WEIGHTED"));
        }
    }

    @Nested
    @DisplayName("Shared formula with POAttainmentCalculator")
    class SharedFormula {

        @Test
        @DisplayName("Same data and mapping produces same result whether PO or PSO calculator is used")
        void sameDataAndMapping_sameResult() {
            // PSO using same values as PO1 spec → should equal 73.83
            List<AttainmentCalculationResult> results = calculator.calculateAll(
                    List.of(new ProgramSpecificOutcomeData(PSO1_ID, PROGRAM_ID, "PSO1", "desc")),
                    dsAttainments(),
                    List.of(
                            COMappingData.toPSO(DS_CO1, "CO1", COURSE_DS, PSO1_ID, 3),
                            COMappingData.toPSO(DS_CO2, "CO2", COURSE_DS, PSO1_ID, 2),
                            COMappingData.toPSO(DS_CO3, "CO3", COURSE_DS, PSO1_ID, 1)
                    ),
                    PROGRAM_ID, "2023-24", "v1");

            assertThat(results.get(0).directAttainment()).isEqualTo(73.83);
        }
    }

    @Test
    @DisplayName("Empty PSO list returns empty results")
    void emptyPSOList_returnsEmpty() {
        List<AttainmentCalculationResult> results = calculator.calculateAll(
                List.of(), dsAttainments(), dsPSOMappings(),
                PROGRAM_ID, "2023-24", "v1");

        assertThat(results).isEmpty();
    }
}
