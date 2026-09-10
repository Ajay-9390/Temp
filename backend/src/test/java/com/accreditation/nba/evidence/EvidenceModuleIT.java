package com.accreditation.nba.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accreditation.nba.evidence.dto.request.CreateEvidenceRequest;
import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.dto.response.EvidenceResponse;
import com.accreditation.nba.evidence.dto.response.StatisticsResponse;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.service.EvidenceService;
import com.accreditation.nba.evidence.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end integration test against a real PostgreSQL (Testcontainers). Flyway migrations
 * run on startup. Requires Docker; executed during {@code mvn verify} (failsafe), not the
 * fast unit {@code mvn test} run.
 */
@SpringBootTest
@Testcontainers
class EvidenceModuleIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private StatisticsService statisticsService;

    @Test
    void createsAndFetchesEvidenceInDraft() {
        CreateEvidenceRequest request = new CreateEvidenceRequest(
                "Integration test evidence", "created by IT", EvidenceCategory.COURSE_FILE,
                null, null, null, null, null, null);

        EvidenceResponse created = evidenceService.create(request, null);
        assertThat(created.status()).isEqualTo(EvidenceStatus.DRAFT);

        EvidenceResponse fetched = evidenceService.get(created.id());
        assertThat(fetched.title()).isEqualTo("Integration test evidence");
        assertThat(fetched.versionCount()).isZero();
    }

    @Test
    void submitWithoutFileFails() {
        EvidenceResponse created = evidenceService.create(new CreateEvidenceRequest(
                "No file", null, EvidenceCategory.OTHER, null, null, null, null, null, null), null);

        assertThatThrownBy(() -> evidenceService.submit(created.id(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listAndStatisticsReflectCreatedEvidence() {
        evidenceService.create(new CreateEvidenceRequest(
                "Listed evidence", null, EvidenceCategory.RESEARCH, null, null, null, null, null, null), null);

        var page = evidenceService.list(
                new EvidenceSearchCriteria(null, null, null, null, null, null, null, null, null, null, null, null),
                PageRequest.of(0, 10));
        assertThat(page.totalElements()).isPositive();

        StatisticsResponse stats = statisticsService.getStatistics(null, null);
        assertThat(stats.totalEvidence()).isPositive();
        assertThat(stats.byStatus().get(EvidenceStatus.DRAFT)).isPositive();
    }
}
