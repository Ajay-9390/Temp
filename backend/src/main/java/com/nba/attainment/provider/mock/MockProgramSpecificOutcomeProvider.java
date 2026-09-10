package com.nba.attainment.provider.mock;

import com.nba.attainment.dto.domain.ProgramSpecificOutcomeData;
import com.nba.attainment.provider.ProgramSpecificOutcomeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nba.attainment.provider.mock.MockDataConstants.*;

/**
 * Mock implementation of {@link ProgramSpecificOutcomeProvider}.
 *
 * <p>Returns three PSOs typical for a B.Tech CSE programme.
 */
@Component
@Profile({"mock", "default"})
public class MockProgramSpecificOutcomeProvider implements ProgramSpecificOutcomeProvider {

    private static final Logger log = LoggerFactory.getLogger(MockProgramSpecificOutcomeProvider.class);

    private static final List<ProgramSpecificOutcomeData> ALL_PSOS = List.of(
        new ProgramSpecificOutcomeData(PSO1_ID, PROGRAM_ID, "PSO1",
            "Apply software engineering principles and practices to develop robust, maintainable and scalable software systems for real-world applications."),
        new ProgramSpecificOutcomeData(PSO2_ID, PROGRAM_ID, "PSO2",
            "Design and implement efficient algorithms and data structures to solve computational problems in areas such as artificial intelligence, data analytics and distributed systems."),
        new ProgramSpecificOutcomeData(PSO3_ID, PROGRAM_ID, "PSO3",
            "Demonstrate proficiency in emerging computing technologies including cloud computing, IoT and cybersecurity to address contemporary industry challenges.")
    );

    private static final Map<UUID, ProgramSpecificOutcomeData> BY_ID =
            ALL_PSOS.stream().collect(Collectors.toMap(ProgramSpecificOutcomeData::psoId, Function.identity()));

    @Override
    public List<ProgramSpecificOutcomeData> getProgramSpecificOutcomes(UUID programId) {
        log.debug("MockProgramSpecificOutcomeProvider: getProgramSpecificOutcomes for program={}", programId);
        return PROGRAM_ID.equals(programId) ? ALL_PSOS : List.of();
    }

    @Override
    public Optional<ProgramSpecificOutcomeData> getProgramSpecificOutcomeById(UUID psoId) {
        log.debug("MockProgramSpecificOutcomeProvider: getProgramSpecificOutcomeById psoId={}", psoId);
        return Optional.ofNullable(BY_ID.get(psoId));
    }
}
