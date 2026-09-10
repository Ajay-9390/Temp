package com.nba.attainment.provider.mock;

import com.nba.attainment.dto.domain.COMappingData;
import com.nba.attainment.provider.MappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.nba.attainment.dto.domain.COMappingData.toPO;
import static com.nba.attainment.dto.domain.COMappingData.toPSO;
import static com.nba.attainment.provider.mock.MockDataConstants.*;

/**
 * Mock implementation of {@link MappingProvider}.
 *
 * <p>CO → PO mapping table (mapping levels 0-3):
 *
 * <pre>
 *                 PO1  PO2  PO3  PO4  PO5  PO6
 * DS-CO1           3    2    1    2    1    0
 * DS-CO2           2    3    2    1    2    1
 * DS-CO3           1    2    3    2    1    2
 *
 * OOP-CO1          2    3    2    1    0    1
 * OOP-CO2          1    2    3    2    1    0
 * OOP-CO3          2    1    2    3    2    1
 *
 * ALGO-CO1         3    2    3    2    2    1
 * ALGO-CO2         2    3    2    3    1    2
 * ALGO-CO3         1    2    3    2    2    3
 * </pre>
 *
 * <p>CO → PSO mapping table:
 *
 * <pre>
 *                 PSO1  PSO2  PSO3
 * DS-CO1           3     2     1
 * DS-CO2           2     3     2
 * DS-CO3           1     2     3
 *
 * OOP-CO1          2     3     2
 * OOP-CO2          3     2     1
 * OOP-CO3          1     3     2
 *
 * ALGO-CO1         3     2     3
 * ALGO-CO2         2     3     2
 * ALGO-CO3         1     2     3
 * </pre>
 */
@Component
@Profile({"mock", "default"})
public class MockMappingProvider implements MappingProvider {

    private static final Logger log = LoggerFactory.getLogger(MockMappingProvider.class);

    private static final List<COMappingData> ALL_PO_MAPPINGS = List.of(

        // ── Data Structures COs → POs ────────────────────────────────────────
        toPO(DS_CO1_ID, "CO1", COURSE_DS_ID, PO1_ID,  3),
        toPO(DS_CO1_ID, "CO1", COURSE_DS_ID, PO2_ID,  2),
        toPO(DS_CO1_ID, "CO1", COURSE_DS_ID, PO3_ID,  1),
        toPO(DS_CO1_ID, "CO1", COURSE_DS_ID, PO4_ID,  2),
        toPO(DS_CO1_ID, "CO1", COURSE_DS_ID, PO5_ID,  1),

        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO1_ID,  2),
        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO2_ID,  3),
        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO3_ID,  2),
        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO4_ID,  1),
        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO5_ID,  2),
        toPO(DS_CO2_ID, "CO2", COURSE_DS_ID, PO6_ID,  1),

        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO1_ID,  1),
        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO2_ID,  2),
        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO3_ID,  3),
        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO4_ID,  2),
        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO5_ID,  1),
        toPO(DS_CO3_ID, "CO3", COURSE_DS_ID, PO6_ID,  2),

        // ── OOP COs → POs ────────────────────────────────────────────────────
        toPO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PO1_ID,  2),
        toPO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PO2_ID,  3),
        toPO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PO3_ID,  2),
        toPO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PO4_ID,  1),
        toPO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PO6_ID,  1),

        toPO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PO1_ID,  1),
        toPO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PO2_ID,  2),
        toPO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PO3_ID,  3),
        toPO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PO4_ID,  2),
        toPO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PO5_ID,  1),

        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO1_ID,  2),
        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO2_ID,  1),
        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO3_ID,  2),
        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO4_ID,  3),
        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO5_ID,  2),
        toPO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PO6_ID,  1),

        // ── Algorithms COs → POs ─────────────────────────────────────────────
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO1_ID,  3),
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO2_ID,  2),
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO3_ID,  3),
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO4_ID,  2),
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO5_ID,  2),
        toPO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PO6_ID,  1),

        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO1_ID,  2),
        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO2_ID,  3),
        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO3_ID,  2),
        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO4_ID,  3),
        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO5_ID,  1),
        toPO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PO6_ID,  2),

        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO1_ID,  1),
        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO2_ID,  2),
        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO3_ID,  3),
        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO4_ID,  2),
        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO5_ID,  2),
        toPO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PO6_ID,  3)
    );

    private static final List<COMappingData> ALL_PSO_MAPPINGS = List.of(

        // ── Data Structures COs → PSOs ───────────────────────────────────────
        toPSO(DS_CO1_ID, "CO1", COURSE_DS_ID, PSO1_ID, 3),
        toPSO(DS_CO1_ID, "CO1", COURSE_DS_ID, PSO2_ID, 2),
        toPSO(DS_CO1_ID, "CO1", COURSE_DS_ID, PSO3_ID, 1),

        toPSO(DS_CO2_ID, "CO2", COURSE_DS_ID, PSO1_ID, 2),
        toPSO(DS_CO2_ID, "CO2", COURSE_DS_ID, PSO2_ID, 3),
        toPSO(DS_CO2_ID, "CO2", COURSE_DS_ID, PSO3_ID, 2),

        toPSO(DS_CO3_ID, "CO3", COURSE_DS_ID, PSO1_ID, 1),
        toPSO(DS_CO3_ID, "CO3", COURSE_DS_ID, PSO2_ID, 2),
        toPSO(DS_CO3_ID, "CO3", COURSE_DS_ID, PSO3_ID, 3),

        // ── OOP COs → PSOs ───────────────────────────────────────────────────
        toPSO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PSO1_ID, 2),
        toPSO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PSO2_ID, 3),
        toPSO(OOP_CO1_ID, "CO1", COURSE_OOP_ID, PSO3_ID, 2),

        toPSO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PSO1_ID, 3),
        toPSO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PSO2_ID, 2),
        toPSO(OOP_CO2_ID, "CO2", COURSE_OOP_ID, PSO3_ID, 1),

        toPSO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PSO1_ID, 1),
        toPSO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PSO2_ID, 3),
        toPSO(OOP_CO3_ID, "CO3", COURSE_OOP_ID, PSO3_ID, 2),

        // ── Algorithms COs → PSOs ────────────────────────────────────────────
        toPSO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PSO1_ID, 3),
        toPSO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PSO2_ID, 2),
        toPSO(ALGO_CO1_ID, "CO1", COURSE_ALGO_ID, PSO3_ID, 3),

        toPSO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PSO1_ID, 2),
        toPSO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PSO2_ID, 3),
        toPSO(ALGO_CO2_ID, "CO2", COURSE_ALGO_ID, PSO3_ID, 2),

        toPSO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PSO1_ID, 1),
        toPSO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PSO2_ID, 2),
        toPSO(ALGO_CO3_ID, "CO3", COURSE_ALGO_ID, PSO3_ID, 3)
    );

    @Override
    public List<COMappingData> getCOtoPOMappings(UUID programId) {
        log.debug("MockMappingProvider: getCOtoPOMappings for program={}", programId);
        // All mock data belongs to PROGRAM_ID; return empty for unknown programs
        return PROGRAM_ID.equals(programId) ? ALL_PO_MAPPINGS : List.of();
    }

    @Override
    public List<COMappingData> getCOtoPSOMappings(UUID programId) {
        log.debug("MockMappingProvider: getCOtoPSOMappings for program={}", programId);
        return PROGRAM_ID.equals(programId) ? ALL_PSO_MAPPINGS : List.of();
    }

    @Override
    public List<COMappingData> getCOtoPOMappingsForCourse(UUID programId, UUID courseId) {
        log.debug("MockMappingProvider: getCOtoPOMappingsForCourse program={} course={}",
                  programId, courseId);
        return ALL_PO_MAPPINGS.stream()
                .filter(m -> m.courseId().equals(courseId))
                .toList();
    }

    @Override
    public List<COMappingData> getCOtoPSOMappingsForCourse(UUID programId, UUID courseId) {
        log.debug("MockMappingProvider: getCOtoPSOMappingsForCourse program={} course={}",
                  programId, courseId);
        return ALL_PSO_MAPPINGS.stream()
                .filter(m -> m.courseId().equals(courseId))
                .toList();
    }
}
