-- ============================================================
-- notification-svc schema
-- ============================================================

-- Transactional outbox
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

CREATE INDEX IF NOT EXISTS idx_notification_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Append-only notification dispatch log.
-- channel: EMAIL | SLACK | TEAMS | PUSH
-- status:  PENDING | SENT | FAILED
CREATE TABLE IF NOT EXISTS notification_logs (
    id              UUID         PRIMARY KEY,
    recipient_id    UUID         NOT NULL,
    channel         VARCHAR(20)  NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    subject         VARCHAR(500),
    body            TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMPTZ,
    failure_reason  TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notif_log_recipient  ON notification_logs (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notif_log_status     ON notification_logs (status);
CREATE INDEX IF NOT EXISTS idx_notif_log_event_type ON notification_logs (event_type);

-- Per-member channel routing preferences.
CREATE TABLE IF NOT EXISTS notification_preferences (
    id          UUID         PRIMARY KEY,
    member_id   UUID         NOT NULL,
    channel     VARCHAR(20)  NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (member_id, channel, event_type)
);

CREATE INDEX IF NOT EXISTS idx_notif_pref_member ON notification_preferences (member_id);

