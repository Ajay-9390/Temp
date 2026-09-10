package com.nba.attainment.dto.response;

import java.util.List;

/**
 * Response returned by the combined calculate endpoint.
 */
public record CombinedAttainmentResponse(
        List<POAttainmentResponse>  poResults,
        List<PSOAttainmentResponse> psoResults
) {}
