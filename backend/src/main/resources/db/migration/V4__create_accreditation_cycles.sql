-- AccreditationCycle: belongs to a program. Tier stored as string for extensibility.
CREATE TABLE accreditation_cycle (
    id                UUID           NOT NULL,
    program_id        UUID           NOT NULL,
    name              VARCHAR(150)   NOT NULL,
    tier              VARCHAR(50)    NOT NULL,
    framework_version VARCHAR(100)   NOT NULL,
    application_year  INTEGER        NOT NULL,
    start_date        DATE,
    end_date          DATE,
    status            VARCHAR(20)    NOT NULL,
    remarks           VARCHAR(2000),
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    created_by        VARCHAR(128),
    updated_by        VARCHAR(128),
    version           BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_accreditation_cycle PRIMARY KEY (id),
    CONSTRAINT fk_cycle_program FOREIGN KEY (program_id) REFERENCES program (id)
);

CREATE INDEX idx_cycle_program_id ON accreditation_cycle (program_id);
CREATE INDEX idx_cycle_status ON accreditation_cycle (status);
