-- =====================================================================
-- Custom business audit log
-- =====================================================================
CREATE TABLE audit_log (
    id             UUID          NOT NULL,
    user_id        VARCHAR(128),
    username       VARCHAR(128),
    entity         VARCHAR(64)   NOT NULL,
    entity_id      VARCHAR(64)   NOT NULL,
    action         VARCHAR(32)   NOT NULL,
    old_value      TEXT,
    new_value      TEXT,
    correlation_id VARCHAR(64),
    created_at     TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);
CREATE INDEX idx_audit_log_entity ON audit_log (entity, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);

-- =====================================================================
-- Hibernate Envers revision metadata
-- =====================================================================
CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    username VARCHAR(128),
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

-- =====================================================================
-- Envers *_aud history tables (audit-metadata columns are @NotAudited)
-- rev + revtype are Envers bookkeeping columns; PK is (rev, id).
-- =====================================================================
CREATE TABLE institution_aud (
    id            UUID        NOT NULL,
    rev           INTEGER     NOT NULL,
    revtype       SMALLINT,
    name          VARCHAR(200),
    code          VARCHAR(50),
    type          VARCHAR(100),
    address       VARCHAR(500),
    city          VARCHAR(100),
    state         VARCHAR(100),
    country       VARCHAR(100),
    website       VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    status        VARCHAR(20),
    CONSTRAINT pk_institution_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_institution_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE department_aud (
    id             UUID        NOT NULL,
    rev            INTEGER     NOT NULL,
    revtype        SMALLINT,
    institution_id UUID,
    name           VARCHAR(200),
    code           VARCHAR(50),
    description    VARCHAR(1000),
    hod_user_id    VARCHAR(128),
    status         VARCHAR(20),
    CONSTRAINT pk_department_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_department_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE program_aud (
    id               UUID        NOT NULL,
    rev              INTEGER     NOT NULL,
    revtype          SMALLINT,
    department_id    UUID,
    name             VARCHAR(200),
    code             VARCHAR(50),
    degree           VARCHAR(100),
    branch           VARCHAR(150),
    description      VARCHAR(2000),
    duration_years   INTEGER,
    total_semesters  INTEGER,
    intake           INTEGER,
    established_year INTEGER,
    status           VARCHAR(20),
    CONSTRAINT pk_program_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_program_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE accreditation_cycle_aud (
    id                UUID        NOT NULL,
    rev               INTEGER     NOT NULL,
    revtype           SMALLINT,
    program_id        UUID,
    name              VARCHAR(150),
    tier              VARCHAR(50),
    framework_version VARCHAR(100),
    application_year  INTEGER,
    start_date        DATE,
    end_date          DATE,
    status            VARCHAR(20),
    remarks           VARCHAR(2000),
    CONSTRAINT pk_accreditation_cycle_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_cycle_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE academic_year_aud (
    id         UUID        NOT NULL,
    rev        INTEGER     NOT NULL,
    revtype    SMALLINT,
    program_id UUID,
    name       VARCHAR(50),
    start_date DATE,
    end_date   DATE,
    status     VARCHAR(20),
    CONSTRAINT pk_academic_year_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_year_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE semester_aud (
    id               UUID        NOT NULL,
    rev              INTEGER     NOT NULL,
    revtype          SMALLINT,
    academic_year_id UUID,
    semester_number  INTEGER,
    name             VARCHAR(100),
    start_date       DATE,
    end_date         DATE,
    status           VARCHAR(20),
    CONSTRAINT pk_semester_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_semester_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
