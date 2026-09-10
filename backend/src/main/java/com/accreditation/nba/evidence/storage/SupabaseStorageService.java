package com.accreditation.nba.evidence.storage;

import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.exception.StorageException;
import com.accreditation.nba.evidence.storage.support.StoragePaths;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Supabase Storage implementation (default runtime provider). Uses the Storage REST API with
 * the service key. Files are stored once per version; signed URLs are issued for preview/download.
 *
 * <p>Configure {@code evidence.storage.supabase.url}, {@code .bucket} and {@code .service-key}
 * (never commit the key — see {@code .env.example}).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "evidence.storage", name = "provider", havingValue = "supabase")
public class SupabaseStorageService implements EvidenceStorageService {

    private final EvidenceProperties properties;
    private final RestClient restClient;

    public SupabaseStorageService(EvidenceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
        log.info("Evidence storage: SUPABASE bucket '{}'", properties.getStorage().getSupabase().getBucket());
    }

    @Override
    public StoredObject store(StoreCommand command) {
        EvidenceProperties.Storage.Supabase cfg = requireConfig();
        String objectPath = StoragePaths.buildObjectPath(
                properties.getStorage().getRootPrefix(),
                command.programId(), command.academicYearId(), command.criterionId(),
                command.evidenceId(), command.versionNumber(), command.fileName());
        MediaType contentType = command.contentType() != null
                ? MediaType.parseMediaType(command.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        try {
            restClient.post()
                    .uri(URI.create(objectUrl(cfg, objectPath)))
                    .headers(this::authHeaders)
                    .header("x-upsert", "false")
                    .contentType(contentType)
                    .body(command.content())
                    .retrieve()
                    .toBodilessEntity();
            return new StoredObject(objectPath, command.content().length, provider());
        } catch (RuntimeException e) {
            throw new StorageException("Supabase upload failed for " + objectPath, e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        EvidenceProperties.Storage.Supabase cfg = requireConfig();
        try {
            byte[] bytes = restClient.get()
                    .uri(URI.create(objectUrl(cfg, storagePath)))
                    .headers(this::authHeaders)
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null) {
                throw new StorageException("Empty response loading " + storagePath);
            }
            return new ByteArrayResource(bytes);
        } catch (StorageException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StorageException("Supabase download failed for " + storagePath, e);
        }
    }

    @Override
    public boolean exists(String storagePath) {
        EvidenceProperties.Storage.Supabase cfg = requireConfig();
        try {
            return restClient.method(HttpMethod.HEAD)
                    .uri(URI.create(objectUrl(cfg, storagePath)))
                    .headers(this::authHeaders)
                    .exchange((request, response) -> response.getStatusCode().is2xxSuccessful());
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void delete(String storagePath) {
        EvidenceProperties.Storage.Supabase cfg = requireConfig();
        try {
            restClient.delete()
                    .uri(URI.create(objectUrl(cfg, storagePath)))
                    .headers(this::authHeaders)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            throw new StorageException("Supabase delete failed for " + storagePath, e);
        }
    }

    @Override
    public Optional<String> signedUrl(String storagePath, Duration ttl) {
        EvidenceProperties.Storage.Supabase cfg = requireConfig();
        long seconds = ttl != null ? ttl.toSeconds() : cfg.getSignedUrlTtlSeconds();
        String url = trimEnd(cfg.getUrl()) + "/storage/v1/object/sign/"
                + cfg.getBucket() + "/" + storagePath;
        try {
            SignResponse resp = restClient.post()
                    .uri(URI.create(url))
                    .headers(this::authHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"expiresIn\":" + seconds + "}")
                    .retrieve()
                    .body(SignResponse.class);
            if (resp == null || resp.signedUrl() == null) {
                return Optional.empty();
            }
            return Optional.of(trimEnd(cfg.getUrl()) + "/storage/v1" + resp.signedUrl());
        } catch (RuntimeException e) {
            log.warn("Failed to create Supabase signed URL for {}: {}", storagePath, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String provider() {
        return "supabase";
    }

    private void authHeaders(HttpHeaders headers) {
        String key = properties.getStorage().getSupabase().getServiceKey();
        headers.setBearerAuth(key);
        headers.set("apikey", key);
    }

    private String objectUrl(EvidenceProperties.Storage.Supabase cfg, String objectPath) {
        return trimEnd(cfg.getUrl()) + "/storage/v1/object/" + cfg.getBucket() + "/" + objectPath;
    }

    private EvidenceProperties.Storage.Supabase requireConfig() {
        EvidenceProperties.Storage.Supabase cfg = properties.getStorage().getSupabase();
        if (isBlank(cfg.getUrl()) || isBlank(cfg.getServiceKey()) || isBlank(cfg.getBucket())) {
            throw new StorageException("Supabase storage is not configured "
                    + "(evidence.storage.supabase.url/bucket/service-key)");
        }
        return cfg;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimEnd(String value) {
        String v = value.trim();
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    private record SignResponse(@JsonProperty("signedURL") String signedUrl) {
    }
}
