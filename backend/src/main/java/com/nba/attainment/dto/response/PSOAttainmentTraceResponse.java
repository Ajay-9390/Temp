package com.nba.attainment.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full drill-down trace for a single PSO attainment.
 */
public record PSOAttainmentTraceResponse(
        UUID                  id,
        UUID                  programId,
        UUID                  psoId,
        String                psoCode,
        String                psoDescription,
        String                academicYear,
        Double                directAttainment,
        Double                indirectAttainment,
        Double                finalAttainment,
        String                calculationMethod,
        String                calculationVersion,
        String                formula,
        double                totalWeight,
        double                weightedSum,
        List<CourseBreakdown> courseBreakdown,
        Instant               createdAt
) {

    public record CourseBreakdown(
            UUID              courseId,
            String            courseName,
            List<CODetail>    cos
    ) {}

    public record CODetail(
            UUID   coId,
            String coCode,
            double attainment,
            int    mappingLevel,
            double weightedValue
    ) {}
}
