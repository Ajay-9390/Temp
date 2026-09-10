package com.accreditation.nba.evidence.entity;

import com.accreditation.nba.evidence.enums.OcrStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

/**
 * An immutable file version of an evidence item (table {@code nba_evidence_version}).
 *
 * <p>The physical file is stored once per version in object storage; this row holds only
 * metadata + the storage path + checksum. Replacing a file always creates a new version;
 * existing versions are never overwritten, preserving full history for audit.
 */
@Entity
@Table(name = "nba_evidence_version",
        uniqueConstraints = @UniqueConstraint(name = "uq_version_evidence_number",
                columnNames = {"evidence_id", "version_number"}),
        indexes = @Index(name = "idx_version_evidence", columnList = "evidence_id"))
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NbaEvidenceVersion {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Column(name = "uploaded_by", nullable = false, length = 120)
    private String uploadedBy;

    @Column(name = "change_reason", columnDefinition = "text")
    private String changeReason;

    // ---- Extraction / OCR results (populated asynchronously) ----
    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "detected_language", length = 32)
    private String detectedLanguage;

    @Column(name = "extracted_text", columnDefinition = "text")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", length = 20)
    private OcrStatus ocrStatus;

    @Column(name = "ocr_processed_at")
    private OffsetDateTime ocrProcessedAt;
    // -------------------------------------------------------------

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
