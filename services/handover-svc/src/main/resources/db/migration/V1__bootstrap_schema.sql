-- ============================================================
-- handover-svc schema
-- ============================================================

-- Transactional outbox (CDC / polling relay)
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

CREATE INDEX IF NOT EXISTS idx_handover_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Handover reports: an outgoing engineer documents the state of their shift
-- before the incoming engineer takes over.
-- Status flow:  PENDING → SUBMITTED → ACKNOWLEDGED
--                                   ↘ REJECTED
CREATE TABLE IF NOT EXISTS handover_reports (
    id               UUID         PRIMARY KEY,
    assignment_id    UUID         NOT NULL,
    from_member_id   UUID         NOT NULL,
    to_member_id     UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    summary          TEXT,
    notes            TEXT,
    rejection_reason TEXT,
    scheduled_at     TIMESTAMPTZ  NOT NULL,
    submitted_at     TIMESTAMPTZ,
    acknowledged_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_handover_assignment_id  ON handover_reports (assignment_id);
CREATE INDEX IF NOT EXISTS idx_handover_from_member_id ON handover_reports (from_member_id);
CREATE INDEX IF NOT EXISTS idx_handover_to_member_id   ON handover_reports (to_member_id);
CREATE INDEX IF NOT EXISTS idx_handover_status         ON handover_reports (status);

