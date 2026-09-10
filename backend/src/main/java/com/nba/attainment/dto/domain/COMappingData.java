package com.nba.attainment.dto.domain;

import java.util.UUID;

/**
 * Domain contract for a single CO → PO or CO → PSO mapping entry.
 *
 * <p>Mapping levels follow NBA convention:
 * <ul>
 *   <li>0 = No correlation</li>
 *   <li>1 = Low correlation</li>
 *   <li>2 = Medium correlation</li>
 *   <li>3 = High correlation</li>
 * </ul>
 *
 * <p>Either {@code poId} or {@code psoId} will be non-null, but not both.
 * Use {@link #isToPO()} / {@link #isPSO()} to distinguish.
 *
 * @param coId         Source course outcome
 * @param coCode       Short code, e.g. "CO1"
 * @param courseId     The course this CO belongs to
 * @param poId         Target PO (null if this is a PSO mapping)
 * @param psoId        Target PSO (null if this is a PO mapping)
 * @param mappingLevel Correlation level [0, 3]
 */
public record COMappingData(
        UUID   coId,
        String coCode,
        UUID   courseId,
        UUID   poId,
        UUID   psoId,
        int    mappingLevel
) {

    public COMappingData {
        if (coId   == null)  throw new IllegalArgumentException("coId must not be null");
        if (courseId == null) throw new IllegalArgumentException("courseId must not be null");
        if (poId == null && psoId == null)
            throw new IllegalArgumentException("Either poId or psoId must be non-null");
        if (poId != null && psoId != null)
            throw new IllegalArgumentException("Only one of poId or psoId may be set");
        if (mappingLevel < 0 || mappingLevel > 3)
            throw new IllegalArgumentException("mappingLevel must be in [0, 3], got: " + mappingLevel);
    }

    /** True when this entry maps a CO to a Program Outcome. */
    public boolean isToPO()  { return poId  != null; }

    /** True when this entry maps a CO to a Program Specific Outcome. */
    public boolean isPSO()   { return psoId != null; }

    /** Factory for CO → PO mappings. */
    public static COMappingData toPO(UUID coId, String coCode, UUID courseId,
                                     UUID poId, int mappingLevel) {
        return new COMappingData(coId, coCode, courseId, poId, null, mappingLevel);
    }

    /** Factory for CO → PSO mappings. */
    public static COMappingData toPSO(UUID coId, String coCode, UUID courseId,
                                      UUID psoId, int mappingLevel) {
        return new COMappingData(coId, coCode, courseId, null, psoId, mappingLevel);
    }
}
