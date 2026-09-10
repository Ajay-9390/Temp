package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.dto.response.VersionResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import com.accreditation.nba.evidence.enums.AuditAction;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.enums.OcrStatus;
import com.accreditation.nba.evidence.event.EvidenceDomainEvent;
import com.accreditation.nba.evidence.event.EvidenceEventType;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.exception.EvidenceNotFoundException;
import com.accreditation.nba.evidence.extraction.DocumentExtractionService;
import com.accreditation.nba.evidence.extraction.ExtractionResult;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.ocr.OcrService;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceVersionRepository;
import com.accreditation.nba.evidence.storage.EvidenceStorageService;
import com.accreditation.nba.evidence.storage.StoreCommand;
import com.accreditation.nba.evidence.storage.StoredObject;
import com.accreditation.nba.evidence.validation.FileValidationService;
import com.accreditation.nba.evidence.validation.ValidatedFile;
import com.accreditation.nba.evidence.workflow.EvidenceWorkflow;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manages evidence file versions: validated upload &rarr; store once &rarr; extract metadata
 * &rarr; queue async OCR. A new upload always creates a new version (files are never
 * overwritten), preserving history for audit.
 */
@Slf4j
@Service
public class VersionService {

    private final NbaEvidenceRepository evidenceRepository;
    private final NbaEvidenceVersionRepository versionRepository;
    private final FileValidationService validationService;
    private final EvidenceStorageService storageService;
    private final DocumentExtractionService extractionService;
    private final OcrService ocrService;
    private final EvidenceMapper mapper;
    private final EvidenceWorkflow workflow;
    private final EvidenceAuditService auditService;
    private final DomainEventPublisher eventPublisher;
    private final CurrentUserProvider currentUser;
    private final EvidenceProperties properties;

    public VersionService(NbaEvidenceRepository evidenceRepository,
                          NbaEvidenceVersionRepository versionRepository,
                          FileValidationService validationService,
                          EvidenceStorageService storageService,
                          DocumentExtractionService extractionService,
                          OcrService ocrService,
                          EvidenceMapper mapper,
                          EvidenceWorkflow workflow,
                          EvidenceAuditService auditService,
                          DomainEventPublisher eventPublisher,
                          CurrentUserProvider currentUser,
                          EvidenceProperties properties) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.validationService = validationService;
        this.storageService = storageService;
        this.extractionService = extractionService;
        this.ocrService = ocrService;
        this.mapper = mapper;
        this.workflow = workflow;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
        this.properties = properties;
    }

    @Transactional
    public VersionResponse createVersion(UUID evidenceId, MultipartFile file, String changeReason) {
        NbaEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));
        if (evidence.getStatus() == EvidenceStatus.ARCHIVED) {
            throw new BadRequestException("Cannot add a version to archived evidence");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }

        byte[] content = readBytes(file);
        ValidatedFile validated = validationService.validate(content, file.getOriginalFilename());

        int nextVersion = evidence.getCurrentVersion() + 1;
        StoredObject stored = storageService.store(new StoreCommand(
                content, validated.fileName(), validated.mimeType(),
                evidence.getProgramId(), evidence.getAcademicYearId(), evidence.getCriterionId(),
                evidence.getId(), nextVersion));

        ExtractionResult extraction = extractionService.extract(
                content, validated.extension(), validated.mimeType());
        OcrStatus ocrStatus = determineOcrStatus(extraction);

        NbaEvidenceVersion version = NbaEvidenceVersion.builder()
                .id(UUID.randomUUID())
                .evidenceId(evidence.getId())
                .versionNumber(nextVersion)
                .fileName(validated.fileName())
                .fileType(validated.extension())
                .mimeType(validated.mimeType())
                .fileSize(validated.size())
                .storagePath(stored.storagePath())
                .checksum(validated.checksum())
                .uploadedBy(currentUser.currentUserId())
                .changeReason(changeReason)
                .pageCount(extraction.pageCount())
                .detectedLanguage(extraction.detectedLanguage())
                .extractedText(extraction.text())
                .ocrStatus(ocrStatus)
                .build();
        versionRepository.save(version);

        // Uploading a new version to already-decided evidence starts a revision (back to DRAFT).
        if (evidence.getStatus() == EvidenceStatus.APPROVED || evidence.getStatus() == EvidenceStatus.REJECTED) {
            evidence.setStatus(EvidenceStatus.DRAFT);
        }
        evidence.setCurrentVersion(nextVersion);
        evidenceRepository.save(evidence);

        auditService.record(evidence.getId(), AuditAction.EVIDENCE_VERSION_CREATED,
                "NbaEvidenceVersion", version.getId(), null,
                "v" + nextVersion + " " + validated.fileName(), changeReason);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.VERSION_CREATED,
                evidence.getId(), currentUser.currentUserId(),
                Map.of("versionNumber", nextVersion, "fileName", validated.fileName())));

        if (ocrStatus == OcrStatus.PENDING) {
            ocrService.processVersionAsync(version.getId());
        }
        return mapper.toVersionResponse(version);
    }

    @Transactional(readOnly = true)
    public List<VersionResponse> listVersions(UUID evidenceId) {
        requireEvidence(evidenceId);
        return versionRepository.findByEvidenceIdOrderByVersionNumberDesc(evidenceId).stream()
                .map(mapper::toVersionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VersionResponse getVersion(UUID evidenceId, int versionNumber) {
        return mapper.toVersionResponse(getVersionEntity(evidenceId, versionNumber));
    }

    @Transactional(readOnly = true)
    public DownloadableFile download(UUID evidenceId, int versionNumber) {
        NbaEvidenceVersion version = getVersionEntity(evidenceId, versionNumber);
        return new DownloadableFile(
                storageService.load(version.getStoragePath()),
                version.getFileName(),
                version.getMimeType(),
                version.getFileSize());
    }

    @Transactional(readOnly = true)
    public Optional<String> signedUrl(UUID evidenceId, int versionNumber, Duration ttl) {
        NbaEvidenceVersion version = getVersionEntity(evidenceId, versionNumber);
        return storageService.signedUrl(version.getStoragePath(), ttl);
    }

    private OcrStatus determineOcrStatus(ExtractionResult extraction) {
        if (extraction.hasText()) {
            return OcrStatus.COMPLETED;
        }
        if (extraction.needsOcr() && properties.getOcr().isEnabled()) {
            return OcrStatus.PENDING;
        }
        return OcrStatus.SKIPPED;
    }

    private NbaEvidenceVersion getVersionEntity(UUID evidenceId, int versionNumber) {
        requireEvidence(evidenceId);
        return versionRepository.findByEvidenceIdAndVersionNumber(evidenceId, versionNumber)
                .orElseThrow(() -> new EvidenceNotFoundException(
                        "Version " + versionNumber + " not found for evidence " + evidenceId));
    }

    private void requireEvidence(UUID evidenceId) {
        if (!evidenceRepository.existsById(evidenceId)) {
            throw new EvidenceNotFoundException(evidenceId);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file: " + e.getMessage());
        }
    }
}
