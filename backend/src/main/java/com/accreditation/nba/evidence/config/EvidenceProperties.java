package com.accreditation.nba.evidence.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the Evidence Management module ({@code evidence.*}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "evidence")
public class EvidenceProperties {

    private Storage storage = new Storage();
    private Validation validation = new Validation();
    private Ocr ocr = new Ocr();
    private Ai ai = new Ai();

    @Getter
    @Setter
    public static class Storage {
        /** {@code local} or {@code supabase}. */
        private String provider = "local";
        /** Top-level folder/prefix used inside the bucket or base directory. */
        private String rootPrefix = "nba-evidence";
        private Local local = new Local();
        private Supabase supabase = new Supabase();

        @Getter
        @Setter
        public static class Local {
            private String basePath = "./data/evidence-store";
        }

        @Getter
        @Setter
        public static class Supabase {
            private String url;
            private String bucket = "nba-evidence";
            private String serviceKey;
            private long signedUrlTtlSeconds = 3600;
        }
    }

    @Getter
    @Setter
    public static class Validation {
        private long maxFileSizeBytes = 52_428_800L; // 50 MB
        private List<String> allowedExtensions =
                List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png", "csv");
    }

    @Getter
    @Setter
    public static class Ocr {
        private boolean enabled = false;
        private String tessdataPath;
        private String languages = "eng";
    }

    @Getter
    @Setter
    public static class Ai {
        private boolean enabled = false;
    }
}
