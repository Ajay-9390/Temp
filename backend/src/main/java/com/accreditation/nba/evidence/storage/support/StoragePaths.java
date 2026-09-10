package com.accreditation.nba.evidence.storage.support;

import java.util.UUID;

/**
 * Builds provider-agnostic object paths using the module's storage template:
 * <pre>{rootPrefix}/{programId}/{academicYearId}/{criterionId}/{evidenceId}/{version}_{fileName}</pre>
 * Null external IDs are rendered as {@code general} so drafting works before references exist.
 * The file name is expected to be already sanitised by the validation layer.
 */
public final class StoragePaths {

    private static final String UNASSIGNED = "general";

    private StoragePaths() {
    }

    public static String buildObjectPath(String rootPrefix,
                                         UUID programId,
                                         UUID academicYearId,
                                         UUID criterionId,
                                         UUID evidenceId,
                                         int versionNumber,
                                         String sanitizedFileName) {
        return String.join("/",
                trimSlashes(rootPrefix),
                segment(programId),
                segment(academicYearId),
                segment(criterionId),
                segment(evidenceId),
                versionNumber + "_" + sanitizedFileName);
    }

    private static String segment(UUID id) {
        return id == null ? UNASSIGNED : id.toString();
    }

    private static String trimSlashes(String value) {
        String v = value == null ? "" : value.trim();
        while (v.startsWith("/")) {
            v = v.substring(1);
        }
        while (v.endsWith("/")) {
            v = v.substring(0, v.length() - 1);
        }
        return v.isBlank() ? "nba-evidence" : v;
    }
}
