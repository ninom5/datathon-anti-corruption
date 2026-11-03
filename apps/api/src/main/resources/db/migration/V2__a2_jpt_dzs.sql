-- Issue #40: old EOJN register report (format class A2), JPT register of public bodies, DZS census 2021.
-- Deviations from docs/DATA_MODEL.md §2 are explained in work/decisions.md (2026-11-03).

-- A2 = old EOJN register report (18 columns, supplier name and OIB in one cell, no eojn.hr link).
ALTER TABLE source_file DROP CONSTRAINT source_file_format_class_check;
ALTER TABLE source_file ALTER COLUMN format_class TYPE TEXT;
ALTER TABLE source_file ADD CONSTRAINT source_file_format_class_check
  CHECK (format_class IN ('A','A2','B','C','D'));

-- JPT context on organizations that appear in contracts (docs/DATA_MODEL.md §2, organization).
ALTER TABLE organization
  ADD COLUMN jpt_code TEXT,
  ADD COLUMN jpt_type TEXT,
  ADD COLUMN founders JSONB;

-- Snapshot of the JPT register (jpt.gov.hr, PopisOznakaNaDanasnjiDan). Kept apart from organization so that
-- 4.678 public bodies do not show up as buyers; organizations are enriched from it by OIB.
CREATE TABLE jpt_body (
  oib               CHAR(11) PRIMARY KEY,
  jpt_code          TEXT NOT NULL,
  name              TEXT NOT NULL,
  name_norm         TEXT NOT NULL,
  jpt_type          TEXT NOT NULL,
  county            TEXT,
  municipality_name TEXT,
  founded_on        DATE,
  founders          JSONB NOT NULL DEFAULT '[]',  -- [{"oib": "...", "naziv": "..."}]
  provenance_id     BIGINT NOT NULL REFERENCES provenance(id)
);
CREATE INDEX jpt_body_name_norm ON jpt_body (name_norm);
CREATE INDEX jpt_body_founders ON jpt_body USING gin (founders jsonb_path_ops);

-- JLS with DZS population 2021 and peer group (ANOMALY_DETECTION §3A). Keyed by OIB from JPT instead of
-- organization_id: most of the 556 JLS have no contracts and must not become buyer rows.
CREATE TABLE municipality (
  oib              CHAR(11) PRIMARY KEY,
  name             TEXT NOT NULL,
  kind             TEXT NOT NULL CHECK (kind IN ('grad','opcina','grad_zagreb')),
  county           TEXT NOT NULL,
  population_2021  INT,
  peer_group       TEXT CHECK (peer_group IN ('P1','P2','P3','P4','P5','P6','P7')),
  provenance_id    BIGINT NOT NULL REFERENCES provenance(id)
);
