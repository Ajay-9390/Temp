-- Semester: belongs to an academic year. Number unique within academic year.
CREATE TABLE semester (
    id                UUID           NOT NULL,
    academic_year_id  UUID           NOT NULL,
    semester_number   INTEGER        NOT NULL,
    name              VARCHAR(100)   NOT NULL,
    start_date        DATE,
    end_date          DATE,
    status            VARCHAR(20)    NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    created_by        VARCHAR(128),
    updated_by        VARCHAR(128),
    version           BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_semester PRIMARY KEY (id),
    CONSTRAINT fk_semester_year FOREIGN KEY (academic_year_id) REFERENCES academic_year (id),
    CONSTRAINT uq_semester_year_number UNIQUE (academic_year_id, semester_number)
);

CREATE INDEX idx_semester_year_id ON semester (academic_year_id);
CREATE INDEX idx_semester_number ON semester (semester_number);
