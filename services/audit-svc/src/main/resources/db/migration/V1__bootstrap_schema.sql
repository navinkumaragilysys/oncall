-- ============================================================
-- audit-svc schema
-- ============================================================

-- Transactional outbox (kept for schema consistency; audit itself is the record)
CREATE TABLE IF NOT EXISTS outbox_events (
    id                UUID PRIMARY KEY,
    aggregate_type    VARCHAR(100) NOT NULL,
    aggregate_id      UUID         NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    payload           JSONB        NOT NULL,
    occurred_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at      TIMESTAMPTZ,
    retry_count       INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_audit_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Immutable audit trail — append-only; no UPDATE or DELETE permitted via API.
-- actor_type: MEMBER | SYSTEM | SERVICE
CREATE TABLE IF NOT EXISTS audit_trail (
    id             UUID         PRIMARY KEY,
    actor_id       UUID,
    actor_type     VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    action         VARCHAR(200) NOT NULL,
    resource_type  VARCHAR(100) NOT NULL,
    resource_id    UUID         NOT NULL,
    old_value      JSONB,
    new_value      JSONB,
    ip_address     VARCHAR(50),
    service_name   VARCHAR(100),
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_actor_id     ON audit_trail (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_resource     ON audit_trail (resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_action       ON audit_trail (action);
CREATE INDEX IF NOT EXISTS idx_audit_occurred_at  ON audit_trail (occurred_at DESC);

