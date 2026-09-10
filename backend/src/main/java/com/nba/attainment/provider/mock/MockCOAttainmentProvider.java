package com.nba.attainment.provider.mock;

import com.nba.attainment.dto.domain.COAttainmentData;
import com.nba.attainment.provider.COAttainmentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.nba.attainment.provider.mock.MockDataConstants.*;

/**
 * Mock implementation of {@link COAttainmentProvider}.
 *
 * <p>Returns fixed, realistic CO attainment values for three courses in
 * the B.Tech CSE program for 2023-24. This implementation is active on
 * the "mock" and "default" (no active profile) Spring profiles.
 *
 * <p>Replace this with {@code RealCOAttainmentProvider} to consume live
 * data from the CO Attainment module — no other code needs to change.
 *
 * <pre>
 * Data Structures
 *   CO1 = 75%  CO2 = 68%  CO3 = 82%
 *
 * OOP
 *   CO1 = 71%  CO2 = 79%  CO3 = 65%
 *
 * Algorithms
 *   CO1 = 80%  CO2 = 73%  CO3 = 77%
 * </pre>
 */
@Component
@Profile({"mock", "default"})
public class MockCOAttainmentProvider implements COAttainmentProvider {

    private static final Logger log = LoggerFactory.getLogger(MockCOAttainmentProvider.class);

    /** All mock CO attainment records indexed once at construction. */
    private static final List<COAttainmentData> ALL_RECORDS = List.of(

        // ── Data Structures ──────────────────────────────────────────────────
        new COAttainmentData(PROGRAM_ID, COURSE_DS_ID, COURSE_DS_NAME,
                             DS_CO1_ID, "CO1", ACADEMIC_YEAR, 75.0),
        new COAttainmentData(PROGRAM_ID, COURSE_DS_ID, COURSE_DS_NAME,
                             DS_CO2_ID, "CO2", ACADEMIC_YEAR, 68.0),
        new COAttainmentData(PROGRAM_ID, COURSE_DS_ID, COURSE_DS_NAME,
                             DS_CO3_ID, "CO3", ACADEMIC_YEAR, 82.0),

        // ── Object-Oriented Programming ───────────────────────────────────────
        new COAttainmentData(PROGRAM_ID, COURSE_OOP_ID, COURSE_OOP_NAME,
                             OOP_CO1_ID, "CO1", ACADEMIC_YEAR, 71.0),
        new COAttainmentData(PROGRAM_ID, COURSE_OOP_ID, COURSE_OOP_NAME,
                             OOP_CO2_ID, "CO2", ACADEMIC_YEAR, 79.0),
        new COAttainmentData(PROGRAM_ID, COURSE_OOP_ID, COURSE_OOP_NAME,
                             OOP_CO3_ID, "CO3", ACADEMIC_YEAR, 65.0),

        // ── Design & Analysis of Algorithms ───────────────────────────────────
        new COAttainmentData(PROGRAM_ID, COURSE_ALGO_ID, COURSE_ALGO_NAME,
                             ALGO_CO1_ID, "CO1", ACADEMIC_YEAR, 80.0),
        new COAttainmentData(PROGRAM_ID, COURSE_ALGO_ID, COURSE_ALGO_NAME,
                             ALGO_CO2_ID, "CO2", ACADEMIC_YEAR, 73.0),
        new COAttainmentData(PROGRAM_ID, COURSE_ALGO_ID, COURSE_ALGO_NAME,
                             ALGO_CO3_ID, "CO3", ACADEMIC_YEAR, 77.0)
    );

    @Override
    public List<COAttainmentData> getAttainments(UUID programId, String academicYear) {
        log.debug("MockCOAttainmentProvider: fetching attainments for program={} year={}",
                  programId, academicYear);
        return ALL_RECORDS.stream()
                .filter(r -> r.programId().equals(programId)
                          && r.academicYear().equals(academicYear))
                .toList();
    }

    @Override
    public List<COAttainmentData> getAttainmentsForCourse(UUID programId, UUID courseId,
                                                          String academicYear) {
        log.debug("MockCOAttainmentProvider: fetching attainments for program={} course={} year={}",
                  programId, courseId, academicYear);
        return ALL_RECORDS.stream()
                .filter(r -> r.programId().equals(programId)
                          && r.courseId().equals(courseId)
                          && r.academicYear().equals(academicYear))
                .toList();
    }
}
