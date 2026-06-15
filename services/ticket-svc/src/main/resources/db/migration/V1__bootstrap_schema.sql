-- ============================================================
-- ticket-svc schema
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

CREATE INDEX IF NOT EXISTS idx_ticket_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Per-team Azure DevOps organisation configuration.
CREATE TABLE IF NOT EXISTS ado_org_configs (
    id                   UUID         PRIMARY KEY,
    team_id              UUID         NOT NULL UNIQUE,
    org_url              VARCHAR(500) NOT NULL,
    project              VARCHAR(200) NOT NULL,
    area_path            VARCHAR(500),
    iteration_path       VARCHAR(500),
    default_work_item_type VARCHAR(100) NOT NULL DEFAULT 'Task',
    active               BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Records of Azure DevOps tickets created for on-call incidents.
-- ticket_status: CREATED | ACTIVE | RESOLVED | CLOSED
CREATE TABLE IF NOT EXISTS devops_tickets (
    id             UUID         PRIMARY KEY,
    team_id        UUID         NOT NULL,
    assignment_id  UUID,
    worklog_id     UUID,
    ado_ticket_id  BIGINT       NOT NULL,
    ado_url        VARCHAR(1000) NOT NULL,
    ticket_type    VARCHAR(100) NOT NULL,
    summary        VARCHAR(500) NOT NULL,
    ticket_status  VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    synced_at      TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ticket_team_id       ON devops_tickets (team_id);
CREATE INDEX IF NOT EXISTS idx_ticket_assignment_id ON devops_tickets (assignment_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status        ON devops_tickets (ticket_status);

