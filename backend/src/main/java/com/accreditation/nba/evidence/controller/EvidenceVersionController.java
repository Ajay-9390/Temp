package com.accreditation.nba.evidence.controller;

import com.accreditation.nba.evidence.dto.response.VersionResponse;
import com.accreditation.nba.evidence.service.DownloadableFile;
import com.accreditation.nba.evidence.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Evidence Versions", description = "Upload and retrieve versioned evidence files")
@RestController
@RequestMapping("/api/v1/evidences/{id}/versions")
public class EvidenceVersionController {

    private final VersionService versionService;

    public EvidenceVersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @Operation(summary = "List versions (newest first)")
    @GetMapping
    public List<VersionResponse> list(@PathVariable UUID id) {
        return versionService.listVersions(id);
    }

    @Operation(summary = "Get a specific version")
    @GetMapping("/{versionNumber}")
    public VersionResponse get(@PathVariable UUID id, @PathVariable int versionNumber) {
        return versionService.getVersion(id, versionNumber);
    }

    @Operation(summary = "Upload a new version (multipart file). Never overwrites previous versions.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VersionResponse> create(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "changeReason", required = false) String changeReason) {
        VersionResponse response = versionService.createVersion(id, file, changeReason);
        return ResponseEntity
                .created(URI.create("/api/v1/evidences/" + id + "/versions/" + response.versionNumber()))
                .body(response);
    }

    @Operation(summary = "Download a version (redirects to a signed URL when available)")
    @GetMapping("/{versionNumber}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id, @PathVariable int versionNumber) {
        Optional<String> signed = versionService.signedUrl(id, versionNumber, null);
        if (signed.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(signed.get())).build();
        }
        DownloadableFile file = versionService.download(id, versionNumber);
        return streamResponse(file, "attachment");
    }

    @Operation(summary = "Preview a version inline (redirects to a signed URL when available)")
    @GetMapping("/{versionNumber}/preview")
    public ResponseEntity<Resource> preview(@PathVariable UUID id, @PathVariable int versionNumber) {
        Optional<String> signed = versionService.signedUrl(id, versionNumber, null);
        if (signed.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(signed.get())).build();
        }
        DownloadableFile file = versionService.download(id, versionNumber);
        return streamResponse(file, "inline");
    }

    private ResponseEntity<Resource> streamResponse(DownloadableFile file, String disposition) {
        MediaType mediaType = file.mimeType() != null
                ? MediaType.parseMediaType(file.mimeType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + file.fileName() + "\"")
                .contentLength(file.size())
                .body(file.resource());
    }
}
