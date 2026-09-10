-- Institution: root of the ownership hierarchy.
CREATE TABLE institution (
    id             UUID           NOT NULL,
    name           VARCHAR(200)   NOT NULL,
    code           VARCHAR(50)    NOT NULL,
    type           VARCHAR(100),
    address        VARCHAR(500),
    city           VARCHAR(100),
    state          VARCHAR(100),
    country        VARCHAR(100),
    website        VARCHAR(255),
    contact_email  VARCHAR(255),
    contact_phone  VARCHAR(50),
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL,
    created_by     VARCHAR(128),
    updated_by     VARCHAR(128),
    version        BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_institution PRIMARY KEY (id),
    CONSTRAINT uq_institution_code UNIQUE (code)
);

CREATE INDEX idx_institution_status ON institution (status);
