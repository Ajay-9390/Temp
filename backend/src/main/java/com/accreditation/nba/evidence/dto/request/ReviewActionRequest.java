package com.accreditation.nba.evidence.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Reviewer action payload for approve / reject / request-changes. Comments are mandatory
 * for reject and request-changes (enforced in the service based on the decision).
 * {@code reviewer} is optional; it falls back to the mock identity when omitted.
 */
public record ReviewActionRequest(

        @Size(max = 120)
        String reviewer,

        @Size(max = 5000)
        String comments
) {
}
