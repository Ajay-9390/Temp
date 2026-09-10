package com.nba.attainment.provider;

import com.nba.attainment.dto.domain.COMappingData;

import java.util.List;
import java.util.UUID;

/**
 * Contract for obtaining CO → PO and CO → PSO mapping data.
 *
 * <p>Mappings are owned by the CO-PO-PSO Mapping module. This interface
 * is the seam that keeps this module decoupled from their internals.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code MockMappingProvider} — fixed realistic data for development</li>
 *   <li>{@code RealMappingProvider} — adapter to the mapping module's data</li>
 * </ul>
 */
public interface MappingProvider {

    /**
     * Returns all CO → PO mappings for a program.
     *
     * @param programId the program whose mappings are requested
     * @return list of CO → PO mapping entries; never null
     */
    List<COMappingData> getCOtoPOMappings(UUID programId);

    /**
     * Returns all CO → PSO mappings for a program.
     *
     * @param programId the program whose mappings are requested
     * @return list of CO → PSO mapping entries; never null
     */
    List<COMappingData> getCOtoPSOMappings(UUID programId);

    /**
     * Returns CO → PO mappings filtered to a specific course.
     *
     * @param programId the program
     * @param courseId  the specific course
     * @return mappings for COs belonging to that course
     */
    List<COMappingData> getCOtoPOMappingsForCourse(UUID programId, UUID courseId);

    /**
     * Returns CO → PSO mappings filtered to a specific course.
     *
     * @param programId the program
     * @param courseId  the specific course
     * @return mappings for COs belonging to that course
     */
    List<COMappingData> getCOtoPSOMappingsForCourse(UUID programId, UUID courseId);
}
