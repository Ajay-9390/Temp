package com.accreditation.nba.evidence.service;

import org.springframework.core.io.Resource;

/**
 * A file ready to be streamed to the client (used when the storage provider does not issue
 * direct signed URLs, e.g. the local filesystem provider).
 */
public record DownloadableFile(
        Resource resource,
        String fileName,
        String mimeType,
        long size
) {
}
