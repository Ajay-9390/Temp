-- =====================================================================================
-- NBA Evidence Management — owned schema (module: com.accreditation.nba.evidence)
-- =====================================================================================
-- Scope: ONLY the five tables owned by this module. External entities (Program,
-- Department, Academic Year, NBA Criterion, Requirement) are referenced by UUID with NO
-- cross-module foreign keys. Do not add other modules' tables here and do not edit other
-- developers' migrations.
-- =====================================================================================

-- ---------------------------------------------------------------------------
-- Root evidence aggregate
-- ---------------------------------------------------------------------------
CREATE TABLE nba_evidence (
    id                 UUID         NOT NULL,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    category           VARCHAR(40)  NOT NULL,
    -- External module references (IDs only, no FK) --
    program_id         UUID,
    department_id      UUID,
    academic_year_id   UUID,
    criterion_id       UUID,
    requirement_id     UUID,
    -- Lifecycle --
    status             VARCHAR(40)  NOT NULL DEFAULT 'DRAFT',
    current_version    INTEGER      NOT NULL DEFAULT 0,
    tags               TEXT,
    uploaded_by        VARCHAR(120) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_nba_evidence PRIMARY KEY (id)
);

CREATE INDEX idx_evidence_program       ON nba_evidence (program_id);
CREATE INDEX idx_evidence_academic_year ON nba_evidence (academic_year_id);
CREATE INDEX idx_evidence_criterion     ON nba_evidence (criterion_id);
CREATE INDEX idx_evidence_status        ON nba_evidence (status);
CREATE INDEX idx_evidence_category      ON nba_evidence (category);

-- ---------------------------------------------------------------------------
-- Immutable file versions (physical file stored once per version in object storage)
-- ---------------------------------------------------------------------------
CREATE TABLE nba_evidence_version (
    id                 UUID          NOT NULL,
    evidence_id        UUID          NOT NULL,
    version_number     INTEGER       NOT NULL,
    file_name          VARCHAR(255)  NOT NULL,
    file_type          VARCHAR(20),
    mime_type          VARCHAR(150),
    file_size          BIGINT        NOT NULL,
    storage_path       VARCHAR(1024) NOT NULL,
    checksum           VARCHAR(128),
    uploaded_by        VARCHAR(120)  NOT NULL,
    change_reason      TEXT,
    -- Extraction / OCR (populated asynchronously) --
    page_count         INTEGER,
    detected_language  VARCHAR(32),
    extracted_text     TEXT,
    ocr_status         VARCHAR(20),
    ocr_processed_at   TIMESTAMPTZ,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_nba_evidence_version PRIMARY KEY (id),
    CONSTRAINT uq_version_evidence_number UNIQUE (evidence_id, version_number),
    CONSTRAINT fk_version_evidence FOREIGN KEY (evidence_id)
        REFERENCES nba_evidence (id) ON DELETE CASCADE
);

CREATE INDEX idx_version_evidence ON nba_evidence_version (evidence_id);

-- ---------------------------------------------------------------------------
-- Evidence <-> criterion/requirement mappings (one evidence supports many requirements)
-- ---------------------------------------------------------------------------
CREATE TABLE nba_evidence_mapping (
    id                 UUID         NOT NULL,
    evidence_id        UUID         NOT NULL,
    criterion_id       UUID         NOT NULL,
    requirement_id     UUID         NOT NULL,
    mapping_type       VARCHAR(30)  NOT NULL,
    created_by         VARCHAR(120),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_nba_evidence_mapping PRIMARY KEY (id),
    CONSTRAINT uq_mapping_evidence_criterion_requirement
        UNIQUE (evidence_id, criterion_id, requirement_id),
    CONSTRAINT fk_mapping_evidence FOREIGN KEY (evidence_id)
        REFERENCES nba_evidence (id) ON DELETE CASCADE
);

CREATE INDEX idx_mapping_evidence    ON nba_evidence_mapping (evidence_id);
CREATE INDEX idx_mapping_criterion   ON nba_evidence_mapping (criterion_id);
CREATE INDEX idx_mapping_requirement ON nba_evidence_mapping (requirement_id);

-- ---------------------------------------------------------------------------
-- Review history (approve / reject / request-changes)
-- ---------------------------------------------------------------------------
CREATE TABLE nba_evidence_review (
    id                 UUID         NOT NULL,
    evidence_id        UUID         NOT NULL,
    version_number     INTEGER,
    reviewer           VARCHAR(120) NOT NULL,
    decision           VARCHAR(30)  NOT NULL,
    resulting_status   VARCHAR(40)  NOT NULL,
    comments           TEXT,
    reviewed_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_nba_evidence_review PRIMARY KEY (id),
    CONSTRAINT fk_review_evidence FOREIGN KEY (evidence_id)
        REFERENCES nba_evidence (id) ON DELETE CASCADE
);

CREATE INDEX idx_review_evidence ON nba_evidence_review (evidence_id);

-- ---------------------------------------------------------------------------
-- Evidence-scoped business audit trail (custom; complements Hibernate Envers)
-- ---------------------------------------------------------------------------
CREATE TABLE nba_evidence_audit (
    id                 UUID         NOT NULL,
    evidence_id        UUID         NOT NULL,
    action             VARCHAR(50)  NOT NULL,
    entity_type        VARCHAR(60)  NOT NULL,
    entity_id          UUID,
    performed_by       VARCHAR(120) NOT NULL,
    old_value          TEXT,
    new_value          TEXT,
    reason             TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_nba_evidence_audit PRIMARY KEY (id),
    CONSTRAINT fk_audit_evidence FOREIGN KEY (evidence_id)
        REFERENCES nba_evidence (id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_evidence ON nba_evidence_audit (evidence_id);
CREATE INDEX idx_audit_action   ON nba_evidence_audit (action);
