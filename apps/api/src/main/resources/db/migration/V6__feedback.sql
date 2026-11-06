-- #45: corrections and the right of reply (docs/DATA_MODEL.md, docs/FEATURES.md F12).
-- Nothing is shown publicly before manual moderation; the e-mail is never shown and is deleted after 12 months.
CREATE TABLE feedback (
    id            BIGSERIAL PRIMARY KEY,
    target_type   TEXT        NOT NULL CHECK (target_type IN ('indicator', 'ai_assessment', 'contract', 'organization')),
    target_id     BIGINT      NOT NULL,
    kind          TEXT        NOT NULL CHECK (kind IN ('greska_u_podacima', 'kontekst', 'pravo_na_odgovor', 'korisno',
                                                       'pogresno', 'uklanjanje_osobnih_podataka')),
    role          TEXT CHECK (role IN ('gradanin', 'novinar', 'narucitelj', 'dobavljac', 'ostalo')),
    text          TEXT,
    contact_email TEXT,
    moderation    TEXT        NOT NULL DEFAULT 'novo' CHECK (moderation IN ('novo', 'objavljeno', 'odbijeno')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX feedback_target_idx ON feedback (target_type, target_id);
