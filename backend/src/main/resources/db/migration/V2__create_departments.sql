-- Department: belongs to an institution. Code unique within institution.
CREATE TABLE department (
    id              UUID           NOT NULL,
    institution_id  UUID           NOT NULL,
    name            VARCHAR(200)   NOT NULL,
    code            VARCHAR(50)    NOT NULL,
    description     VARCHAR(1000),
    hod_user_id     VARCHAR(128),
    status          VARCHAR(20)    NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL,
    updated_at      TIMESTAMPTZ    NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_department PRIMARY KEY (id),
    CONSTRAINT fk_department_institution FOREIGN KEY (institution_id) REFERENCES institution (id),
    CONSTRAINT uq_department_institution_code UNIQUE (institution_id, code)
);

CREATE INDEX idx_department_institution_id ON department (institution_id);
CREATE INDEX idx_department_code ON department (code);
CREATE INDEX idx_department_status ON department (status);
