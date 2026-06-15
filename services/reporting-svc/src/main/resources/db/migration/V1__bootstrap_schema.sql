-- ============================================================
-- reporting-svc schema
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

CREATE INDEX IF NOT EXISTS idx_reporting_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Materialised report snapshots.
-- report_type: FAIRNESS_SUMMARY | WORKLOAD_SUMMARY | LEAVE_UTILIZATION |
--              TICKET_SUMMARY | ONCALL_COVERAGE
CREATE TABLE IF NOT EXISTS report_snapshots (
    id             UUID         PRIMARY KEY,
    report_type    VARCHAR(50)  NOT NULL,
    team_id        UUID,
    period_start   DATE         NOT NULL,
    period_end     DATE         NOT NULL,
    data           JSONB        NOT NULL,
    generated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_report_type_team   ON report_snapshots (report_type, team_id);
CREATE INDEX IF NOT EXISTS idx_report_period      ON report_snapshots (period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_report_generated   ON report_snapshots (generated_at DESC);

