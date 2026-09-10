package com.nba.attainment.provider;

import com.nba.attainment.dto.domain.ProgramOutcomeData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract for obtaining Program Outcome (PO) definitions.
 *
 * <p>POs are owned by the PO/PSO Management module. This interface
 * decouples this attainment module from that module's entity model.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@code MockProgramOutcomeProvider} — NBA-standard 12 POs for development</li>
 *   <li>{@code RealProgramOutcomeProvider} — adapter to the management module</li>
 * </ul>
 */
public interface ProgramOutcomeProvider {

    /**
     * Returns all POs defined for the given program.
     *
     * @param programId the program
     * @return list of program outcomes; never null
     */
    List<ProgramOutcomeData> getProgramOutcomes(UUID programId);

    /**
     * Returns a single PO by its identifier.
     *
     * @param poId the PO UUID
     * @return the outcome, or empty if not found
     */
    Optional<ProgramOutcomeData> getProgramOutcomeById(UUID poId);
}
