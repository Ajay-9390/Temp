package com.nba.attainment.dto.response;

import java.util.List;

/**
 * Aggregated summary of all PO and PSO attainments for a program/year.
 * Consumed by the dashboard.
 */
public record AttainmentSummaryResponse(
        List<POAttainmentResponse>  poAttainments,
        List<PSOAttainmentResponse> psoAttainments,
        Double averagePOAttainment,
        Double averagePSOAttainment,
        POAttainmentResponse  highestPO,
        POAttainmentResponse  lowestPO,
        PSOAttainmentResponse highestPSO,
        PSOAttainmentResponse lowestPSO
) {}
