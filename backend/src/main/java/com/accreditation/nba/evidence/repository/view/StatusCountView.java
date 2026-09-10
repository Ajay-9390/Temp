package com.accreditation.nba.evidence.repository.view;

import com.accreditation.nba.evidence.enums.EvidenceStatus;

/** Projection: count of evidence grouped by status. */
public interface StatusCountView {
    EvidenceStatus getStatus();

    long getCount();
}
