-- AcademicYear: belongs to a program. Name unique within program.
CREATE TABLE academic_year (
    id          UUID           NOT NULL,
    program_id  UUID           NOT NULL,
    name        VARCHAR(50)    NOT NULL,
    start_date  DATE           NOT NULL,
    end_date    DATE           NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL,
    created_by  VARCHAR(128),
    updated_by  VARCHAR(128),
    version     BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_academic_year PRIMARY KEY (id),
    CONSTRAINT fk_year_program FOREIGN KEY (program_id) REFERENCES program (id),
    CONSTRAINT uq_academic_year_program_name UNIQUE (program_id, name)
);

CREATE INDEX idx_year_program_id ON academic_year (program_id);
CREATE INDEX idx_year_status ON academic_year (status);
