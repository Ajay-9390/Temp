-- =============================================================
-- PO/PSO Attainment Module — Initial Schema
-- V1__create_attainment_tables.sql
-- =============================================================

-- ---------------------------------------------------------------
-- PO Attainment
-- ---------------------------------------------------------------
CREATE TABLE po_attainment (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    program_id           UUID         NOT NULL,
    po_id                UUID         NOT NULL,
    po_code              VARCHAR(20)  NOT NULL,
    academic_year        VARCHAR(20)  NOT NULL,
    direct_attainment    DOUBLE PRECISION,
    indirect_attainment  DOUBLE PRECISION,
    final_attainment     DOUBLE PRECISION,
    calculation_method   VARCHAR(50)  NOT NULL,
    calculation_version  VARCHAR(20)  NOT NULL,
    calculation_trace    JSONB,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_po_attainment PRIMARY KEY (id),
    CONSTRAINT chk_po_direct   CHECK (direct_attainment   IS NULL OR (direct_attainment   >= 0 AND direct_attainment   <= 100)),
    CONSTRAINT chk_po_indirect CHECK (indirect_attainment IS NULL OR (indirect_attainment >= 0 AND indirect_attainment <= 100)),
    CONSTRAINT chk_po_final    CHECK (final_attainment    IS NULL OR (final_attainment    >= 0 AND final_attainment    <= 100))
);

-- Unique: one record per program + PO + academic year + version
CREATE UNIQUE INDEX uq_po_attainment_current
    ON po_attainment (program_id, po_id, academic_year, calculation_version);

CREATE INDEX idx_po_attainment_program_year
    ON po_attainment (program_id, academic_year);

CREATE INDEX idx_po_attainment_po_id
    ON po_attainment (po_id);

-- ---------------------------------------------------------------
-- PSO Attainment
-- ---------------------------------------------------------------
CREATE TABLE pso_attainment (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    program_id           UUID         NOT NULL,
    pso_id               UUID         NOT NULL,
    pso_code             VARCHAR(20)  NOT NULL,
    academic_year        VARCHAR(20)  NOT NULL,
    direct_attainment    DOUBLE PRECISION,
    indirect_attainment  DOUBLE PRECISION,
    final_attainment     DOUBLE PRECISION,
    calculation_method   VARCHAR(50)  NOT NULL,
    calculation_version  VARCHAR(20)  NOT NULL,
    calculation_trace    JSONB,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_pso_attainment PRIMARY KEY (id),
    CONSTRAINT chk_pso_direct   CHECK (direct_attainment   IS NULL OR (direct_attainment   >= 0 AND direct_attainment   <= 100)),
    CONSTRAINT chk_pso_indirect CHECK (indirect_attainment IS NULL OR (indirect_attainment >= 0 AND indirect_attainment <= 100)),
    CONSTRAINT chk_pso_final    CHECK (final_attainment    IS NULL OR (final_attainment    >= 0 AND final_attainment    <= 100))
);

CREATE UNIQUE INDEX uq_pso_attainment_current
    ON pso_attainment (program_id, pso_id, academic_year, calculation_version);

CREATE INDEX idx_pso_attainment_program_year
    ON pso_attainment (program_id, academic_year);

CREATE INDEX idx_pso_attainment_pso_id
    ON pso_attainment (pso_id);
