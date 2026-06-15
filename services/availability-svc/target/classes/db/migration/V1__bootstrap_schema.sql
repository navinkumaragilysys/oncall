-- V1: Availability service schema
-- Owns leave requests, emergency OOO, midweek reassignments, and swap requests.

CREATE TABLE IF NOT EXISTS leave_requests (
    id                       UUID        PRIMARY KEY,
    member_id                UUID        NOT NULL,
    manager_id               UUID        NOT NULL,
    suggested_replacement_id UUID,
    leave_type               VARCHAR(30) NOT NULL,
    start_date               DATE        NOT NULL,
    end_date                 DATE        NOT NULL,
    description              TEXT,
    status                   VARCHAR(30) NOT NULL,
    rejection_reason         VARCHAR(500),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_leave_member ON leave_requests (member_id);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests (status);

CREATE TABLE IF NOT EXISTS emergency_ooo (
    id                    UUID        PRIMARY KEY,
    member_id             UUID        NOT NULL,
    reported_by_id        UUID        NOT NULL,
    replacement_member_id UUID,
    manager_id            UUID        NOT NULL,
    start_time            TIMESTAMPTZ NOT NULL,
    effective_start_time  TIMESTAMPTZ NOT NULL,
    reason                TEXT,
    status                VARCHAR(30) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_emergency_member ON emergency_ooo (member_id);

CREATE TABLE IF NOT EXISTS midweek_reassignments (
    id                    UUID        PRIMARY KEY,
    assignment_id         UUID        NOT NULL,
    requested_by_id       UUID        NOT NULL,
    original_member_id    UUID        NOT NULL,
    replacement_member_id UUID,
    manager_id            UUID        NOT NULL,
    reason_category       VARCHAR(50) NOT NULL,
    description           TEXT,
    requested_at          TIMESTAMPTZ NOT NULL,
    effective_at          TIMESTAMPTZ,
    status                VARCHAR(30) NOT NULL,
    rejection_reason      VARCHAR(500),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reassign_assignment ON midweek_reassignments (assignment_id);
CREATE INDEX IF NOT EXISTS idx_reassign_member     ON midweek_reassignments (original_member_id);

CREATE TABLE IF NOT EXISTS swap_requests (
    id               UUID        PRIMARY KEY,
    requestor_id     UUID        NOT NULL,
    target_member_id UUID        NOT NULL,
    assignment_id    UUID        NOT NULL,
    manager_id       UUID        NOT NULL,
    reason           TEXT        NOT NULL,
    status           VARCHAR(30) NOT NULL,
    rejection_reason VARCHAR(500),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_swap_requestor ON swap_requests (requestor_id);
CREATE INDEX IF NOT EXISTS idx_swap_target    ON swap_requests (target_member_id);

CREATE TABLE IF NOT EXISTS outbox_events (
    id             UUID         PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB        NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ,
    retry_count    INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_availability_outbox_unpublished
    ON outbox_events (occurred_at) WHERE published_at IS NULL;
