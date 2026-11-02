-- JavniRadar POC schema: subset of docs/DATA_MODEL.md §2.
-- Deliberately omitted until later phases: municipality, company_snapshot, procurement_plan, plan_item,
-- procedure, lot, bid, contract_amendment, payment, ai_assessment, llm_usage, feedback.
-- Additions to DATA_MODEL.md are marked "POC addition" and documented there (§8).

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ===== Operational =====
CREATE TABLE ingest_run (
  id           BIGSERIAL PRIMARY KEY,
  started_at   TIMESTAMPTZ NOT NULL,
  finished_at  TIMESTAMPTZ,
  status       TEXT NOT NULL CHECK (status IN ('running','finished','failed')),
  stats        JSONB
);

-- ===== Provenance =====
CREATE TABLE source_dataset (
  id                 BIGSERIAL PRIMARY KEY,
  portal             TEXT NOT NULL,              -- 'data.gov.hr' | 'synthetic'
  external_id        TEXT NOT NULL,              -- CKAN package name
  title              TEXT,
  publisher          TEXT,
  license_id         TEXT,
  landing_url        TEXT,
  metadata_modified  TIMESTAMPTZ,                -- POC addition: CKAN metadata_modified
  is_synthetic       BOOLEAN NOT NULL DEFAULT false, -- POC addition
  UNIQUE (portal, external_id)
);

CREATE TABLE source_file (
  id              BIGSERIAL PRIMARY KEY,
  dataset_id      BIGINT NOT NULL REFERENCES source_dataset(id),
  resource_id     TEXT,                          -- POC addition: CKAN resource id
  name            TEXT,                          -- POC addition: CKAN resource name
  resource_modified TIMESTAMPTZ,                 -- POC addition: CKAN last_modified/created; decides the "newest file" on dedup
  url             TEXT NOT NULL,
  format          TEXT,
  format_class    CHAR(1) CHECK (format_class IN ('A','B','C','D')),
  sha256          CHAR(64),
  bytes           BIGINT,
  http_status     INT,
  error_message   TEXT,                          -- POC addition: why download/classification failed
  retrieved_at    TIMESTAMPTZ NOT NULL,
  ingest_run_id   BIGINT REFERENCES ingest_run(id),
  UNIQUE (url, sha256)
);

CREATE TABLE raw_record (
  id        BIGSERIAL PRIMARY KEY,
  file_id   BIGINT NOT NULL REFERENCES source_file(id),
  locator   TEXT NOT NULL,                       -- 'sheet=1;row=41' (1-based Excel row)
  payload   JSONB NOT NULL,                      -- original row, header -> cell text, unchanged
  UNIQUE (file_id, locator)
);

CREATE TABLE provenance (
  id                 BIGSERIAL PRIMARY KEY,
  raw_record_id      BIGINT REFERENCES raw_record(id),
  extraction_method  TEXT NOT NULL CHECK (extraction_method IN ('parser','llm','manual','api')),
  extractor_version  TEXT NOT NULL,
  confidence         NUMERIC(3,2) NOT NULL DEFAULT 1.0,
  field_confidence   JSONB,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ingest_issue (
  id             BIGSERIAL PRIMARY KEY,
  run_id         BIGINT REFERENCES ingest_run(id),
  file_id        BIGINT REFERENCES source_file(id),  -- POC addition: file-level issues have no raw record
  raw_record_id  BIGINT REFERENCES raw_record(id),
  severity       TEXT NOT NULL CHECK (severity IN ('error','warning')),
  code           TEXT NOT NULL,
  message        TEXT
);
CREATE INDEX ingest_issue_run ON ingest_issue (run_id, code);

-- ===== Reference data =====
CREATE TABLE organization (
  id             BIGSERIAL PRIMARY KEY,
  oib            CHAR(11) UNIQUE,                -- NULL only when the source has no usable OIB
  name           TEXT NOT NULL,
  name_norm      TEXT NOT NULL,
  org_type       TEXT NOT NULL CHECK (org_type IN
                   ('jls','zupanija','javno_tijelo','doo','jdoo','dd','obrt','udruga','fizicka_osoba','strana','ostalo','nepoznato')),
  county         TEXT,
  is_synthetic   BOOLEAN NOT NULL DEFAULT false,
  provenance_id  BIGINT REFERENCES provenance(id)
);
CREATE INDEX organization_name_trgm ON organization USING gin (name_norm gin_trgm_ops);
CREATE INDEX organization_name_norm ON organization (name_norm) WHERE oib IS NULL;

-- ===== Procurement =====
CREATE TABLE contract (
  id                       BIGSERIAL PRIMARY KEY,
  natural_key              TEXT UNIQUE NOT NULL,   -- class A: EOJN contract URL
  buyer_id                 BIGINT NOT NULL REFERENCES organization(id),
  supplier_id              BIGINT REFERENCES organization(id),
  eojn_contract_url        TEXT,
  eojn_tender_url          TEXT,                   -- POC addition (procedure table comes later)
  framework_agreement_url  TEXT,                   -- POC addition: "Na osnovu okvirnog sporazuma"
  evid_number              TEXT,                   -- POC addition
  contract_number          TEXT,
  subject                  TEXT NOT NULL,
  subject_type             TEXT CHECK (subject_type IN ('robe','usluge','radovi','ostalo')),
  cpv                      CHAR(8),
  cpv_label                TEXT,
  procedure_type           TEXT,
  is_framework_based       BOOLEAN NOT NULL DEFAULT false,
  date_signed              DATE NOT NULL,
  duration_text            TEXT,
  currency_original        CHAR(3) NOT NULL DEFAULT 'EUR',
  initial_value_eur        NUMERIC(14,2),
  initial_value_vat_eur    NUMERIC(14,2),
  amendments_value_eur     NUMERIC(14,2),
  total_value_eur          NUMERIC(14,2),
  total_value_vat_eur      NUMERIC(14,2),
  paid_eur                 NUMERIC(14,2),
  completed_on             DATE,
  payment_note             TEXT,
  note                     TEXT,                   -- POC addition: "Napomena"
  status                   TEXT,
  eu_funded                BOOLEAN,
  quality_flags            TEXT[] NOT NULL DEFAULT '{}', -- POC addition: normalisation warnings, input to data_quality
  is_synthetic             BOOLEAN NOT NULL DEFAULT false,
  provenance_id            BIGINT NOT NULL REFERENCES provenance(id),
  valid_from               TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (total_value_eur IS NULL OR total_value_eur >= 0)
);
CREATE INDEX contract_buyer_date ON contract (buyer_id, date_signed);
CREATE INDEX contract_supplier ON contract (supplier_id);
CREATE INDEX contract_cpv2 ON contract (substr(cpv,1,2));
CREATE INDEX contract_subject_trgm ON contract USING gin (subject gin_trgm_ops);
CREATE INDEX contract_synthetic ON contract (is_synthetic);

-- ===== Analysis =====
CREATE TABLE indicator (
  id                 BIGSERIAL PRIMARY KEY,
  rule_code          TEXT NOT NULL,
  rule_version       TEXT NOT NULL,
  subject_type       TEXT NOT NULL CHECK (subject_type IN ('contract','buyer','supplier','buyer_cpv','group')),
  subject_ref        JSONB NOT NULL,
  contract_id        BIGINT REFERENCES contract(id), -- POC addition: fast "has indicator" filter for contract rules
  magnitude_value    NUMERIC,
  magnitude_unit     TEXT,
  magnitude_label    TEXT,
  data_quality       TEXT NOT NULL CHECK (data_quality IN ('niska','srednja','visoka')),
  evidence_strength  TEXT NOT NULL CHECK (evidence_strength IN ('slaba','srednja','jaka')),
  severity_note      TEXT CHECK (severity_note IN ('informativno','vrijedi_provjeriti')),
  explanation_hr     TEXT NOT NULL,
  not_meaning_hr     TEXT NOT NULL,
  missing_data       TEXT[],
  evidence           JSONB NOT NULL,
  status             TEXT NOT NULL DEFAULT 'aktivan' CHECK (status IN ('aktivan','osporen','ispravljen','povucen')),
  computed_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  input_hash         CHAR(64) NOT NULL,
  UNIQUE (rule_code, rule_version, input_hash)
  -- deliberately no "score"/"risk" column: the three dimensions are never combined
);
CREATE INDEX indicator_contract ON indicator (contract_id);
