-- Program: belongs to a department. Code unique within department.
CREATE TABLE program (
    id               UUID           NOT NULL,
    department_id    UUID           NOT NULL,
    name             VARCHAR(200)   NOT NULL,
    code             VARCHAR(50)    NOT NULL,
    degree           VARCHAR(100),
    branch           VARCHAR(150),
    description      VARCHAR(2000),
    duration_years   INTEGER        NOT NULL,
    total_semesters  INTEGER        NOT NULL,
    intake           INTEGER        NOT NULL,
    established_year INTEGER,
    status           VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL,
    created_by       VARCHAR(128),
    updated_by       VARCHAR(128),
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_program PRIMARY KEY (id),
    CONSTRAINT fk_program_department FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT uq_program_department_code UNIQUE (department_id, code)
);

CREATE INDEX idx_program_department_id ON program (department_id);
CREATE INDEX idx_program_code ON program (code);
CREATE INDEX idx_program_status ON program (status);
