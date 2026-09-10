package com.nba.attainment.provider;

import com.nba.attainment.dto.domain.ProgramSpecificOutcomeData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract for obtaining Program Specific Outcome (PSO) definitions.
 *
 * <p>PSOs are owned by the PO/PSO Management module. This interface
 * decouples this attainment module from that module's entity model.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code MockProgramSpecificOutcomeProvider} — realistic PSOs for development</li>
 *   <li>{@code RealProgramSpecificOutcomeProvider} — adapter to the management module</li>
 * </ul>
 */
public interface ProgramSpecificOutcomeProvider {

    /**
     * Returns all PSOs defined for the given program.
     *
     * @param programId the program
     * @return list of program specific outcomes; never null
     */
    List<ProgramSpecificOutcomeData> getProgramSpecificOutcomes(UUID programId);

    /**
     * Returns a single PSO by its identifier.
     *
     * @param psoId the PSO UUID
     * @return the outcome, or empty if not found
     */
    Optional<ProgramSpecificOutcomeData> getProgramSpecificOutcomeById(UUID psoId);
}
