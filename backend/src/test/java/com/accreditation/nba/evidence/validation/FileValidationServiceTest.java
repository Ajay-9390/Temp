package com.accreditation.nba.evidence.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.exception.FileValidationException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileValidationServiceTest {

    private FileValidationService service;

    @BeforeEach
    void setUp() {
        service = new FileValidationService(new EvidenceProperties());
    }

    @Test
    void acceptsValidPdfAndComputesChecksum() {
        byte[] pdf = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF".getBytes(StandardCharsets.UTF_8);

        ValidatedFile result = service.validate(pdf, "FDP Certificate.pdf");

        assertThat(result.extension()).isEqualTo("pdf");
        assertThat(result.mimeType()).isEqualTo("application/pdf");
        assertThat(result.fileName()).isEqualTo("FDP_Certificate.pdf");
        assertThat(result.size()).isEqualTo(pdf.length);
        assertThat(result.checksum()).hasSize(64); // SHA-256 hex
    }

    @Test
    void acceptsCsv() {
        byte[] csv = "name,score\nAlice,90\nBob,85".getBytes(StandardCharsets.UTF_8);
        ValidatedFile result = service.validate(csv, "results.csv");
        assertThat(result.extension()).isEqualTo("csv");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> service.validate(new byte[0], "empty.pdf"))
                .isInstanceOf(FileValidationException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsUnsupportedExtension() {
        byte[] content = "echo hi".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> service.validate(content, "malware.exe"))
                .isInstanceOf(FileValidationException.class);
    }

    @Test
    void rejectsContentExtensionMismatch() {
        // Plain text content but a .pdf extension -> content does not match extension.
        byte[] text = "this is not a pdf".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> service.validate(text, "fake.pdf"))
                .isInstanceOf(FileValidationException.class);
    }

    @Test
    void sanitizesPathTraversalFileName() {
        String sanitized = service.sanitizeFileName("..\\..\\secret.pdf");
        assertThat(sanitized).isEqualTo("secret.pdf");
    }

    @Test
    void sanitizesUnsafeCharacters() {
        String sanitized = service.sanitizeFileName("my report (final)#1.pdf");
        assertThat(sanitized).matches("[A-Za-z0-9._-]+");
        assertThat(sanitized).endsWith(".pdf");
    }
}
