-- Issue #41: group rules F (splitting) and C (concentration), magnitude band for filters.
-- Deviations from docs/DATA_MODEL.md §2 are explained in work/decisions.md (2026-11-03, #41).

-- Buyer of every indicator, so /indicators and the buyer profile can filter without parsing subject_ref.
ALTER TABLE indicator ADD COLUMN buyer_id BIGINT REFERENCES organization(id);
UPDATE indicator i SET buyer_id = c.buyer_id FROM contract c WHERE c.id = i.contract_id;
CREATE INDEX indicator_buyer ON indicator (buyer_id);

-- JavniRadar heuristic band of the magnitude (thresholds.yaml, "bands"); never a combined risk score.
ALTER TABLE indicator ADD COLUMN magnitude_band TEXT CHECK (magnitude_band IN ('nizak','srednji','visok'));

-- V1 named the index on indicator.contract_id "indicator_contract"; the name is needed for the link table.
ALTER INDEX indicator_contract RENAME TO indicator_contract_id;

-- Every contract an indicator is about: one row for contract rules, all contracts of the group for F and C.
CREATE TABLE indicator_contract (
  indicator_id BIGINT NOT NULL REFERENCES indicator(id) ON DELETE CASCADE,
  contract_id  BIGINT NOT NULL REFERENCES contract(id),
  PRIMARY KEY (indicator_id, contract_id)
);
CREATE INDEX indicator_contract_contract ON indicator_contract (contract_id);
INSERT INTO indicator_contract (indicator_id, contract_id)
  SELECT id, contract_id FROM indicator WHERE contract_id IS NOT NULL;
