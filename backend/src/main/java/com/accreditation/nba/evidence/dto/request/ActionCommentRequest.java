package com.accreditation.nba.evidence.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Generic optional-comment payload for simple lifecycle actions (submit, start-review,
 * archive). May be sent as an empty body.
 */
public record ActionCommentRequest(

        @Size(max = 5000)
        String comment
) {
}
