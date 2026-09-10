-- =====================================================================================
-- Search extension points (PostgreSQL full-text search).
-- =====================================================================================
-- Uses only core PostgreSQL features (safe to run everywhere). Generated tsvector columns
-- + GIN indexes prepare the ground for full-text search. The current SearchService uses
-- portable ILIKE filtering; it can be upgraded to use these columns without a schema change.
--
-- These columns are intentionally NOT mapped in JPA entities (Hibernate ignores unmapped
-- columns), keeping the ORM model clean.
-- =====================================================================================

-- Full-text vector over evidence metadata (title, description, tags).
ALTER TABLE nba_evidence
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector('english',
            coalesce(title, '') || ' ' ||
            coalesce(description, '') || ' ' ||
            coalesce(tags, ''))
    ) STORED;

CREATE INDEX idx_evidence_search_vector ON nba_evidence USING GIN (search_vector);

-- Full-text vector over version file text (file name + extracted/OCR text).
ALTER TABLE nba_evidence_version
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector('english',
            coalesce(file_name, '') || ' ' ||
            coalesce(extracted_text, ''))
    ) STORED;

CREATE INDEX idx_version_search_vector ON nba_evidence_version USING GIN (search_vector);

-- -------------------------------------------------------------------------------------
-- FUTURE (pgvector semantic search) — NOT enabled here because it requires the pgvector
-- extension to be installed on the shared PostgreSQL server. When available, add a new
-- migration (e.g. V3__evidence_embeddings.sql) containing:
--
--   CREATE EXTENSION IF NOT EXISTS vector;
--   ALTER TABLE nba_evidence_version ADD COLUMN embedding vector(1536);
--   CREATE INDEX idx_version_embedding ON nba_evidence_version
--       USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
--
-- The AiEvidenceService extension point is already present in the codebase (no-op default).
-- -------------------------------------------------------------------------------------
