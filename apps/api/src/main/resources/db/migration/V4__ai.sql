-- Issue #44: AI assessment and LLM usage, as in docs/DATA_MODEL.md §2. The feedback table belongs to #45.

CREATE TABLE ai_assessment (
  id                 BIGSERIAL PRIMARY KEY,
  contract_id        BIGINT REFERENCES contract(id),
  label              TEXT NOT NULL CHECK (label IN ('nema_oznake','vrijedi_provjeriti','visoka_pozornost')),
  reasons            JSONB NOT NULL,          -- [{tvrdnja, polja[], izvori[]}]
  missing_data       JSONB NOT NULL,
  legit_explanations JSONB NOT NULL,
  confidence         TEXT NOT NULL CHECK (confidence IN ('niska','srednja','visoka')),
  model              TEXT NOT NULL,           -- 'claude-haiku-5-5'
  prompt_version     TEXT NOT NULL,           -- 'ai-flag-v1'
  input_hash         CHAR(64) NOT NULL,
  status             TEXT NOT NULL CHECK (status IN ('prihvaceno','odbaceno_validacijom','zamijenjeno')),
  rejection_reason   TEXT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (contract_id, model, prompt_version, input_hash)
);
CREATE INDEX ai_assessment_contract ON ai_assessment (contract_id, status);

CREATE TABLE llm_usage (
  id            BIGSERIAL PRIMARY KEY,
  purpose       TEXT,
  model         TEXT,
  input_tokens  INT,
  output_tokens INT,
  cost_usd      NUMERIC(10,4),
  cache_hit     BOOLEAN,
  created_at    TIMESTAMPTZ DEFAULT now()
);
