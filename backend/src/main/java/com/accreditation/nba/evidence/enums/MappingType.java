package com.accreditation.nba.evidence.enums;

/**
 * Nature of the link between an evidence item and an NBA criterion/requirement.
 * One evidence item may be mapped to many requirements with different mapping types.
 */
public enum MappingType {
    /** Directly and primarily satisfies the requirement. */
    PRIMARY,
    /** Supports the requirement alongside other evidence. */
    SUPPORTING,
    /** Additional/contextual evidence. */
    SUPPLEMENTARY
}
