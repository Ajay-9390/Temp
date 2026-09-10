package com.accreditation.nba.evidence.repository.view;

import com.accreditation.nba.evidence.enums.EvidenceCategory;

/** Projection: count of evidence grouped by category. */
public interface CategoryCountView {
    EvidenceCategory getCategory();

    long getCount();
}
