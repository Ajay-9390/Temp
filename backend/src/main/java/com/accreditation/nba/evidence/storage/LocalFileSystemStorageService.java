package com.accreditation.nba.evidence.storage;

import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.exception.StorageException;
import com.accreditation.nba.evidence.storage.support.StoragePaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Filesystem-backed storage for local development and tests. Files are written under
 * {@code evidence.storage.local.base-path}. The default provider when none is configured.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "evidence.storage", name = "provider",
        havingValue = "local", matchIfMissing = true)
public class LocalFileSystemStorageService implements EvidenceStorageService {

    private final EvidenceProperties properties;
    private final Path baseDir;

    public LocalFileSystemStorageService(EvidenceProperties properties) {
        this.properties = properties;
        this.baseDir = Paths.get(properties.getStorage().getLocal().getBasePath())
                .toAbsolutePath().normalize();
        log.info("Evidence storage: LOCAL filesystem at {}", baseDir);
    }

    @Override
    public StoredObject store(StoreCommand command) {
        String objectPath = StoragePaths.buildObjectPath(
                properties.getStorage().getRootPrefix(),
                command.programId(), command.academicYearId(), command.criterionId(),
                command.evidenceId(), command.versionNumber(), command.fileName());
        Path target = resolveSafely(objectPath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, command.content());
            return new StoredObject(objectPath, command.content().length, provider());
        } catch (IOException e) {
            throw new StorageException("Failed to store file at " + objectPath, e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        Path target = resolveSafely(storagePath);
        if (!Files.exists(target)) {
            throw new StorageException("Stored object not found: " + storagePath);
        }
        return new PathResource(target);
    }

    @Override
    public boolean exists(String storagePath) {
        return Files.exists(resolveSafely(storagePath));
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolveSafely(storagePath));
        } catch (IOException e) {
            throw new StorageException("Failed to delete " + storagePath, e);
        }
    }

    @Override
    public Optional<String> signedUrl(String storagePath, Duration ttl) {
        // Local provider streams through the API instead of issuing direct URLs.
        return Optional.empty();
    }

    @Override
    public String provider() {
        return "local";
    }

    /**
     * Resolve the object path under the base directory and guard against path traversal.
     */
    private Path resolveSafely(String objectPath) {
        Path resolved = baseDir.resolve(objectPath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new StorageException("Path traversal detected for: " + objectPath);
        }
        return resolved;
    }
}
