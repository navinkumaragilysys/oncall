CREATE TABLE IF NOT EXISTS oncall_work_logs (
    id                   UUID PRIMARY KEY,
    assignment_id        UUID        NOT NULL,
    member_id            UUID        NOT NULL,
    log_date             DATE        NOT NULL,
    status               VARCHAR(20) NOT NULL,
    total_active_minutes INTEGER     NOT NULL DEFAULT 0,
    daily_summary        TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_worklog_assignment_member_date UNIQUE (assignment_id, member_id, log_date)
);

CREATE INDEX IF NOT EXISTS idx_worklog_assignment ON oncall_work_logs (assignment_id);
CREATE INDEX IF NOT EXISTS idx_worklog_member ON oncall_work_logs (member_id);

CREATE TABLE IF NOT EXISTS work_log_entries (
    id         UUID PRIMARY KEY,
    work_log_id UUID        NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    note       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_worklog_entry_worklog ON work_log_entries (work_log_id, occurred_at);
