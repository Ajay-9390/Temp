package com.accreditation.nba.evidence.storage;

import java.time.Duration;
import java.util.Optional;
import org.springframework.core.io.Resource;

/**
 * Storage abstraction so business logic never depends directly on a specific provider.
 * Implementations: local filesystem (dev/test) and Supabase Storage (default runtime).
 * Future providers (S3, Azure Blob, MinIO) only need a new implementation of this interface.
 *
 * <p>Binary files live here — never in PostgreSQL. The database stores only the returned
 * {@link StoredObject#storagePath()} plus metadata/checksum.
 */
public interface EvidenceStorageService {

    /** Store a file version once. Never overwrites an existing object (new version = new path). */
    StoredObject store(StoreCommand command);

    /** Load the raw bytes of a stored object for streaming/download. */
    Resource load(String storagePath);

    boolean exists(String storagePath);

    void delete(String storagePath);

    /**
     * A time-limited direct URL for preview/download when the provider supports it
     * (e.g. Supabase signed URLs). Empty for providers that require streaming through the API.
     */
    Optional<String> signedUrl(String storagePath, Duration ttl);

    /** Identifier of the active provider ({@code local} / {@code supabase}). */
    String provider();
}
