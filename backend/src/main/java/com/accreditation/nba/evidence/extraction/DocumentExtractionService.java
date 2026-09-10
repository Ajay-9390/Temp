package com.accreditation.nba.evidence.extraction;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

/**
 * Synchronous, best-effort text/metadata extraction at upload time. Never throws to the
 * caller — extraction failure must not fail an upload. PDFs are processed with PDFBox;
 * CSV/plain text is read directly; images and text-less PDFs are flagged for async OCR.
 */
@Slf4j
@Service
public class DocumentExtractionService {

    private static final int MAX_TEXT_CHARS = 1_000_000;

    public ExtractionResult extract(byte[] content, String extension, String mimeType) {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        try {
            if ("pdf".equals(ext) || "application/pdf".equalsIgnoreCase(mimeType)) {
                return extractPdf(content);
            }
            if (ext.equals("csv")) {
                return new ExtractionResult(null, null, truncate(new String(content, StandardCharsets.UTF_8)), false);
            }
            if (isImage(ext)) {
                // No text layer: hand off to OCR.
                return new ExtractionResult(1, null, null, true);
            }
            // Office documents: metadata captured elsewhere; deep text extraction not performed here.
            return ExtractionResult.none();
        } catch (Exception e) {
            log.warn("Extraction failed for .{} ({}): {}", ext, mimeType, e.getMessage());
            return ExtractionResult.none();
        }
    }

    private ExtractionResult extractPdf(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            int pages = document.getNumberOfPages();
            String text;
            try {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
            } catch (Exception e) {
                text = null;
            }
            boolean hasText = text != null && !text.isBlank();
            if (hasText) {
                return new ExtractionResult(pages, null, truncate(text), false);
            }
            // Text-less PDF (likely scanned) → OCR.
            return new ExtractionResult(pages, null, null, true);
        } catch (Exception e) {
            log.warn("PDF extraction failed: {}", e.getMessage());
            return ExtractionResult.none();
        }
    }

    private boolean isImage(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.strip();
        return trimmed.length() > MAX_TEXT_CHARS ? trimmed.substring(0, MAX_TEXT_CHARS) : trimmed;
    }
}
