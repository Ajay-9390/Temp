package com.nba.attainment.provider.mock;

import com.nba.attainment.dto.domain.ProgramOutcomeData;
import com.nba.attainment.provider.ProgramOutcomeProvider;
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
 * Mock implementation of {@link ProgramOutcomeProvider}.
 *
 * <p>Returns the 12 standard NBA Program Outcomes (PO1–PO12) for
 * Engineering programmes as defined by the NBA self-assessment criteria.
 */
@Component
@Profile({"mock", "default"})
public class MockProgramOutcomeProvider implements ProgramOutcomeProvider {

    private static final Logger log = LoggerFactory.getLogger(MockProgramOutcomeProvider.class);

    private static final List<ProgramOutcomeData> ALL_POS = List.of(
        new ProgramOutcomeData(PO1_ID,  PROGRAM_ID, "PO1",
            "Engineering Knowledge: Apply the knowledge of mathematics, science, engineering fundamentals, and an engineering specialisation to the solution of complex engineering problems."),
        new ProgramOutcomeData(PO2_ID,  PROGRAM_ID, "PO2",
            "Problem Analysis: Identify, formulate, research literature, and analyse complex engineering problems reaching substantiated conclusions using first principles of mathematics, natural sciences and engineering sciences."),
        new ProgramOutcomeData(PO3_ID,  PROGRAM_ID, "PO3",
            "Design/Development of Solutions: Design solutions for complex engineering problems and design system components or processes that meet the specified needs with appropriate consideration for public health and safety, cultural, societal, and environmental considerations."),
        new ProgramOutcomeData(PO4_ID,  PROGRAM_ID, "PO4",
            "Conduct Investigations of Complex Problems: Use research-based knowledge and research methods including design of experiments, analysis and interpretation of data, and synthesis of the information to provide valid conclusions."),
        new ProgramOutcomeData(PO5_ID,  PROGRAM_ID, "PO5",
            "Modern Tool Usage: Create, select, and apply appropriate techniques, resources, and modern engineering and IT tools including prediction and modelling to complex engineering activities with an understanding of the limitations."),
        new ProgramOutcomeData(PO6_ID,  PROGRAM_ID, "PO6",
            "The Engineer and Society: Apply reasoning informed by the contextual knowledge to assess societal, health, safety, legal and cultural issues and the consequent responsibilities relevant to the professional engineering practice."),
        new ProgramOutcomeData(PO7_ID,  PROGRAM_ID, "PO7",
            "Environment and Sustainability: Understand the impact of the professional engineering solutions in societal and environmental contexts, and demonstrate the knowledge of, and need for sustainable development."),
        new ProgramOutcomeData(PO8_ID,  PROGRAM_ID, "PO8",
            "Ethics: Apply ethical principles and commit to professional ethics and responsibilities and norms of the engineering practice."),
        new ProgramOutcomeData(PO9_ID,  PROGRAM_ID, "PO9",
            "Individual and Team Work: Function effectively as an individual, and as a member or leader in diverse teams, and in multidisciplinary settings."),
        new ProgramOutcomeData(PO10_ID, PROGRAM_ID, "PO10",
            "Communication: Communicate effectively on complex engineering activities with the engineering community and with society at large, such as, being able to comprehend and write effective reports and design documentation, make effective presentations, and give and receive clear instructions."),
        new ProgramOutcomeData(PO11_ID, PROGRAM_ID, "PO11",
            "Project Management and Finance: Demonstrate knowledge and understanding of the engineering and management principles and apply these to one's own work, as a member and leader in a team, to manage projects and in multidisciplinary environments."),
        new ProgramOutcomeData(PO12_ID, PROGRAM_ID, "PO12",
            "Life-long Learning: Recognise the need for, and have the preparation and ability to engage in independent and life-long learning in the broadest context of technological change.")
    );

    private static final Map<UUID, ProgramOutcomeData> BY_ID =
            ALL_POS.stream().collect(Collectors.toMap(ProgramOutcomeData::poId, Function.identity()));

    @Override
    public List<ProgramOutcomeData> getProgramOutcomes(UUID programId) {
        log.debug("MockProgramOutcomeProvider: getProgramOutcomes for program={}", programId);
        return PROGRAM_ID.equals(programId) ? ALL_POS : List.of();
    }

    @Override
    public Optional<ProgramOutcomeData> getProgramOutcomeById(UUID poId) {
        log.debug("MockProgramOutcomeProvider: getProgramOutcomeById poId={}", poId);
        return Optional.ofNullable(BY_ID.get(poId));
    }
}
