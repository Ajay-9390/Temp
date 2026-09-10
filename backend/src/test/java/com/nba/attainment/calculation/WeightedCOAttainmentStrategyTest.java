package com.nba.attainment.calculation;

import com.nba.attainment.dto.calculation.AttainmentCalculationInput;
import com.nba.attainment.dto.calculation.AttainmentCalculationResult;
import com.nba.attainment.dto.calculation.COContribution;
import com.nba.attainment.exception.CalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link WeightedCOAttainmentStrategy}.
 *
 * <p>No Spring context, no database — pure Java.
 */
class WeightedCOAttainmentStrategyTest {

    private static final UUID PROGRAM_ID = UUID.randomUUID();
    private static final UUID OUTCOME_ID = UUID.randomUUID();
    private static final UUID COURSE_ID  = UUID.randomUUID();

    private WeightedCOAttainmentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WeightedCOAttainmentStrategy();
    }

    // -------------------------------------------------------------------------
    // Basic identity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("strategyName() returns WEIGHTED")
    void strategyName_returnsWeighted() {
        assertThat(strategy.strategyName()).isEqualTo("WEIGHTED");
    }

    // -------------------------------------------------------------------------
    // Core formula — reference values from the spec
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Core formula — spec reference values")
    class CoreFormula {

        @Test
        @DisplayName("PO1 = 73.83% with DS data from the spec")
        void po1_specExample() {
            // CO1=75 m=3, CO2=68 m=2, CO3=82 m=1
            // (75×3 + 68×2 + 82×1) / (3+2+1) = (225+136+82)/6 = 443/6 = 73.83
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 2),
                    co("CO3", 82.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(73.83);
            assertThat(result.finalAttainment()).isEqualTo(73.83);
        }

        @Test
        @DisplayName("Single CO — attainment equals CO attainment regardless of mapping")
        void singleCO_returnsCoAttainment() {
            List<COContribution> contributions = List.of(co("CO1", 65.0, 2));
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(65.0);
        }

        @Test
        @DisplayName("All COs with mapping level 1 — simple average")
        void equalMappingLevels_producesSimpleAverage() {
            // All mapping=1 → simple average = (60+70+80)/3 = 70
            List<COContribution> contributions = List.of(
                    co("CO1", 60.0, 1),
                    co("CO2", 70.0, 1),
                    co("CO3", 80.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(70.0);
        }

        @Test
        @DisplayName("Weighted formula with mapping levels 3-2-1 is not a simple average")
        void weightedIsDifferentFromSimpleAverage() {
            // CO1=90 m=3, CO2=50 m=1 → weighted=(270+50)/(3+1)=320/4=80
            // simple avg = (90+50)/2 = 70
            List<COContribution> contributions = List.of(
                    co("CO1", 90.0, 3),
                    co("CO2", 50.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(80.0);
            assertThat(result.directAttainment()).isNotEqualTo(70.0);
        }
    }

    // -------------------------------------------------------------------------
    // Zero and missing mapping levels
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Zero and missing mappings")
    class ZeroMappings {

        @Test
        @DisplayName("CO with mapping level 0 is excluded from calculation")
        void zeroMapping_isExcluded() {
            // CO1=75 m=3, CO2=68 m=0 (excluded)
            // result = 75 × 3 / 3 = 75
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 0)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(75.0);
        }

        @Test
        @DisplayName("All COs have mapping level 0 — attainment is 0")
        void allZeroMappings_returnsZero() {
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 0),
                    co("CO2", 80.0, 0)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.directAttainment()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Empty contributions list — attainment is 0")
        void emptyContributions_returnsZero() {
            AttainmentCalculationResult result = strategy.calculate(input(List.of()));

            assertThat(result.directAttainment()).isEqualTo(0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Multiple courses
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Multiple courses")
    class MultipleCourses {

        @Test
        @DisplayName("COs from different courses all contribute")
        void multipleCourses_allContribute() {
            UUID course2 = UUID.randomUUID();
            // CO from course1: 70 × 2 = 140
            // CO from course2: 80 × 2 = 160
            // total weight = 4, weighted sum = 300, result = 75
            COContribution c1 = new COContribution(
                    UUID.randomUUID(), "CO1", COURSE_ID, "Course A",
                    70.0, 2);
            COContribution c2 = new COContribution(
                    UUID.randomUUID(), "CO1", course2, "Course B",
                    80.0, 2);
            AttainmentCalculationResult result = strategy.calculate(input(List.of(c1, c2)));

            assertThat(result.directAttainment()).isEqualTo(75.0);
        }

        @Test
        @DisplayName("Course breakdown in trace contains one entry per distinct course")
        void trace_hasOneCourseEntryPerCourse() {
            UUID course2 = UUID.randomUUID();
            COContribution c1 = new COContribution(
                    UUID.randomUUID(), "CO1", COURSE_ID, "Course A", 70.0, 2);
            COContribution c2 = new COContribution(
                    UUID.randomUUID(), "CO1", course2, "Course B", 80.0, 2);

            AttainmentCalculationResult result = strategy.calculate(input(List.of(c1, c2)));

            assertThat(result.trace().courseBreakdown()).hasSize(2);
        }
    }

    // -------------------------------------------------------------------------
    // Rounding
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Rounding")
    class Rounding {

        @Test
        @DisplayName("Result is rounded to 2 decimal places")
        void result_roundedTo2dp() {
            // 443 / 6 = 73.8333... → 73.83
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 2),
                    co("CO3", 82.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            // Verify exactly 2dp
            assertThat(result.directAttainment()).isEqualTo(73.83);
        }
    }

    // -------------------------------------------------------------------------
    // Trace content
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Calculation trace")
    class TraceContent {

        @Test
        @DisplayName("Trace contains correct totalWeight")
        void trace_totalWeight() {
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 2),
                    co("CO3", 82.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            assertThat(result.trace().totalWeight()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("Trace contains correct weightedSum")
        void trace_weightedSum() {
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 2),
                    co("CO3", 82.0, 1)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            // 75×3=225, 68×2=136, 82×1=82 → 443
            assertThat(result.trace().weightedSum()).isEqualTo(443.0);
        }

        @Test
        @DisplayName("Trace formula description is non-empty")
        void trace_formulaDescription() {
            AttainmentCalculationResult result = strategy.calculate(input(List.of(co("CO1", 70.0, 2))));

            assertThat(result.trace().formula()).isNotBlank();
        }

        @Test
        @DisplayName("Each CO in the trace has a correct weightedValue")
        void trace_coWeightedValues() {
            List<COContribution> contributions = List.of(
                    co("CO1", 75.0, 3),
                    co("CO2", 68.0, 2)
            );
            AttainmentCalculationResult result = strategy.calculate(input(contributions));

            var coDetails = result.trace().courseBreakdown().get(0).coContributions();
            assertThat(coDetails).anySatisfy(d ->
                    assertThat(d.weightedValue()).isEqualTo(225.0)); // 75×3
            assertThat(coDetails).anySatisfy(d ->
                    assertThat(d.weightedValue()).isEqualTo(136.0)); // 68×2
        }
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Error cases")
    class ErrorCases {

        @Test
        @DisplayName("Null input throws CalculationException")
        void nullInput_throwsException() {
            assertThatThrownBy(() -> strategy.calculate(null))
                    .isInstanceOf(CalculationException.class)
                    .hasMessageContaining("must not be null");
        }
    }

    // -------------------------------------------------------------------------
    // Edge cases for attainment boundary values
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Boundary attainment values")
    class BoundaryValues {

        @Test
        @DisplayName("100% attainment with any mapping → 100%")
        void perfectAttainment() {
            AttainmentCalculationResult result =
                    strategy.calculate(input(List.of(co("CO1", 100.0, 3))));

            assertThat(result.directAttainment()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("0% attainment with any mapping → 0%")
        void zeroAttainment() {
            AttainmentCalculationResult result =
                    strategy.calculate(input(List.of(co("CO1", 0.0, 3))));

            assertThat(result.directAttainment()).isEqualTo(0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private COContribution co(String code, double attainment, int mappingLevel) {
        return new COContribution(UUID.randomUUID(), code, COURSE_ID,
                                  "Test Course", attainment, mappingLevel);
    }

    private AttainmentCalculationInput input(List<COContribution> contributions) {
        return new AttainmentCalculationInput(
                OUTCOME_ID, "PO1", PROGRAM_ID, "2023-24",
                contributions, "WEIGHTED");
    }
}
