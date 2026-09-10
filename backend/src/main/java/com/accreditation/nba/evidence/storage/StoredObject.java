package com.accreditation.nba.evidence.storage;

/**
 * Result of storing a file: the logical storage path (persisted on the version row) and the
 * number of bytes written. The path is provider-agnostic and resolvable by {@code load}.
 */
public record StoredObject(
        String storagePath,
        long size,
        String provider
) {
}
