package com.accreditation.nba.evidence.storage;

import java.util.UUID;

/**
 * Everything the storage layer needs to persist one file version. The path components mirror
 * the storage template {@code {rootPrefix}/{programId}/{academicYearId}/{criterionId}/{evidenceId}/{version}_{fileName}}.
 * External IDs may be null (evidence can be drafted before all references are assigned).
 */
public record StoreCommand(
        byte[] content,
        String fileName,
        String contentType,
        UUID programId,
        UUID academicYearId,
        UUID criterionId,
        UUID evidenceId,
        int versionNumber
) {
}
