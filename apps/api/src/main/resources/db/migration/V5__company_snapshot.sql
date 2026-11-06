-- Issue #46 (Sudreg): company register snapshots, as in docs/DATA_MODEL.md §2. A snapshot is never overwritten; the
-- newest one per organization is used. Added to §2: NKD name and version (Sudreg answers with NKD 2025 subclasses),
-- in_liquidation, status_text, and not_found for OIBs the register does not know (not retried every run).

CREATE TABLE company_snapshot (
  id              BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organization(id),
  source          TEXT NOT NULL CHECK (source IN ('sudreg','fixture')),
  retrieved_at    TIMESTAMPTZ NOT NULL,
  not_found       BOOLEAN NOT NULL DEFAULT false,
  mbs             TEXT,
  founded_on      DATE,
  dissolved_on    DATE,
  legal_form      TEXT,
  main_nkd        TEXT,
  main_nkd_name   TEXT,
  nkd_version     TEXT,
  in_bankruptcy   BOOLEAN,
  in_liquidation  BOOLEAN,
  status_text     TEXT,
  payload         JSONB
);
CREATE INDEX company_snapshot_organization ON company_snapshot (organization_id, retrieved_at DESC);
