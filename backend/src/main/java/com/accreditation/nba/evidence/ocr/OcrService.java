package com.accreditation.nba.evidence.ocr;

import com.accreditation.nba.evidence.config.AsyncConfig;
import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import com.accreditation.nba.evidence.enums.OcrStatus;
import com.accreditation.nba.evidence.repository.NbaEvidenceVersionRepository;
import com.accreditation.nba.evidence.storage.EvidenceStorageService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous OCR for scanned PDFs and images using Tesseract (Tess4J). Runs on the
 * dedicated {@link AsyncConfig#OCR_EXECUTOR} so uploads never wait for OCR.
 *
 * <p>OCR is optional: it is disabled by default and the native Tesseract library may be
 * absent. Every failure mode is caught (including {@link Throwable} for native link errors)
 * and recorded as {@link OcrStatus#FAILED}/{@link OcrStatus#SKIPPED} without affecting the
 * stored file or the API.
 */
@Slf4j
@Service
public class OcrService {

    private static final int MAX_OCR_PAGES = 20;
    private static final float RENDER_DPI = 300f;

    private final EvidenceProperties properties;
    private final EvidenceStorageService storageService;
    private final NbaEvidenceVersionRepository versionRepository;

    public OcrService(EvidenceProperties properties,
                      EvidenceStorageService storageService,
                      NbaEvidenceVersionRepository versionRepository) {
        this.properties = properties;
        this.storageService = storageService;
        this.versionRepository = versionRepository;
    }

    @Async(AsyncConfig.OCR_EXECUTOR)
    public void processVersionAsync(UUID versionId) {
        NbaEvidenceVersion version = versionRepository.findById(versionId).orElse(null);
        if (version == null) {
            return;
        }
        if (!properties.getOcr().isEnabled()) {
            markStatus(version, OcrStatus.SKIPPED, null, null);
            return;
        }

        markStatus(version, OcrStatus.PROCESSING, null, null);
        try {
            byte[] content = storageService.load(version.getStoragePath())
                    .getContentAsByteArray();
            String text = runOcr(content, version.getFileType());
            if (text == null || text.isBlank()) {
                markStatus(version, OcrStatus.COMPLETED, "", firstLanguage());
            } else {
                markStatus(version, OcrStatus.COMPLETED, text.strip(), firstLanguage());
            }
            log.debug("OCR completed for version {}", versionId);
        } catch (Throwable t) {
            // Includes UnsatisfiedLinkError / NoClassDefFoundError when native tesseract is absent.
            log.warn("OCR failed for version {}: {}", versionId, t.toString());
            markStatus(version, OcrStatus.FAILED, null, null);
        }
    }

    private String runOcr(byte[] content, String fileType) throws Exception {
        ITesseract tesseract = newTesseract();
        String ext = fileType == null ? "" : fileType.toLowerCase(Locale.ROOT);
        if ("pdf".equals(ext)) {
            return ocrPdf(content, tesseract);
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
        if (image == null) {
            return null;
        }
        return tesseract.doOCR(image);
    }

    private String ocrPdf(byte[] content, ITesseract tesseract) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = Math.min(document.getNumberOfPages(), MAX_OCR_PAGES);
            for (int i = 0; i < pages; i++) {
                BufferedImage page = renderer.renderImageWithDPI(i, RENDER_DPI);
                sb.append(tesseract.doOCR(page)).append('\n');
            }
        }
        return sb.toString();
    }

    private ITesseract newTesseract() {
        Tesseract tesseract = new Tesseract();
        String tessdata = properties.getOcr().getTessdataPath();
        if (tessdata != null && !tessdata.isBlank()) {
            tesseract.setDatapath(tessdata);
        }
        tesseract.setLanguage(properties.getOcr().getLanguages());
        return tesseract;
    }

    private String firstLanguage() {
        String langs = properties.getOcr().getLanguages();
        if (langs == null || langs.isBlank()) {
            return null;
        }
        return langs.split("[+,]")[0].trim();
    }

    private void markStatus(NbaEvidenceVersion version, OcrStatus status, String text, String language) {
        version.setOcrStatus(status);
        if (text != null) {
            version.setExtractedText(text);
        }
        if (language != null) {
            version.setDetectedLanguage(language);
        }
        if (status == OcrStatus.COMPLETED || status == OcrStatus.FAILED || status == OcrStatus.SKIPPED) {
            version.setOcrProcessedAt(OffsetDateTime.now());
        }
        versionRepository.save(version);
    }
}
