-- V2: Refresh token rotation support
-- Stores a SHA-256 hex-digest of the current refresh token in member_credentials.
-- On POST /auth/refresh the incoming token is hashed and compared with the stored digest;
-- a match rotates the token (new digest replaces old).  A mismatch signals a reuse
-- attack — the stored token is immediately cleared.
-- On POST /auth/logout the stored digest is cleared, invalidating any outstanding
-- refresh token for that member.

ALTER TABLE member_credentials
    ADD COLUMN IF NOT EXISTS refresh_token_hash       VARCHAR(64),
    ADD COLUMN IF NOT EXISTS refresh_token_expires_at TIMESTAMPTZ;

-- Partial index — only non-null rows need fast lookup
CREATE INDEX idx_cred_refresh_hash
    ON member_credentials (refresh_token_hash)
    WHERE refresh_token_hash IS NOT NULL;
