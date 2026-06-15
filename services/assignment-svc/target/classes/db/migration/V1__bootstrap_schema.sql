-- V1: Assignment service schema
-- Owns on-call assignments per session and completed history records.

CREATE TABLE IF NOT EXISTS oncall_assignments (
    id                   UUID             PRIMARY KEY,
    session_id           UUID             NOT NULL,
    member_id            UUID             NOT NULL,
    team_id              UUID             NOT NULL,
    role                 VARCHAR(20)      NOT NULL,
    shift_start          TIMESTAMPTZ      NOT NULL,
    shift_end            TIMESTAMPTZ      NOT NULL,
    source               VARCHAR(40)      NOT NULL,
    status               VARCHAR(30)      NOT NULL,
    fairness_credit_days DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_assignments_session ON oncall_assignments (session_id);
CREATE INDEX IF NOT EXISTS idx_assignments_member  ON oncall_assignments (member_id);
CREATE INDEX IF NOT EXISTS idx_assignments_team    ON oncall_assignments (team_id);
CREATE INDEX IF NOT EXISTS idx_assignments_status  ON oncall_assignments (status);

CREATE TABLE IF NOT EXISTS oncall_history (
    id                   UUID             PRIMARY KEY,
    member_id            UUID             NOT NULL,
    assignment_id        UUID             NOT NULL UNIQUE,
    period_start         TIMESTAMPTZ      NOT NULL,
    period_end           TIMESTAMPTZ      NOT NULL,
    role                 VARCHAR(20)      NOT NULL,
    completion_status    VARCHAR(30)      NOT NULL,
    handover_status      VARCHAR(30),
    fairness_credit_days DOUBLE PRECISION NOT NULL DEFAULT 0,
    notes                TEXT,
    imported             BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_history_member     ON oncall_history (member_id);
CREATE INDEX IF NOT EXISTS idx_history_assignment ON oncall_history (assignment_id);

CREATE TABLE IF NOT EXISTS outbox_events (
    id             UUID        PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID        NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB       NOT NULL,
    occurred_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ,
    retry_count    INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_assignment_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;
