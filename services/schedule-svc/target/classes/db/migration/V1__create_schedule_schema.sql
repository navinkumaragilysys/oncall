-- V1: Schedule service schema
-- Owns on-call session lifecycle and schedule query data.

CREATE TABLE IF NOT EXISTS oncall_sessions (
    id                        UUID PRIMARY KEY,
    team_id                   UUID        NOT NULL,
    policy_id                 UUID        NOT NULL,
    sub_session_definition_id UUID,
    rotation_week_start       TIMESTAMPTZ NOT NULL,
    rotation_week_end         TIMESTAMPTZ NOT NULL,
    status                    VARCHAR(30) NOT NULL,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_schedule_team_start
    ON oncall_sessions (team_id, rotation_week_start);

CREATE INDEX IF NOT EXISTS idx_schedule_status
    ON oncall_sessions (status);

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

CREATE INDEX IF NOT EXISTS idx_schedule_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;
