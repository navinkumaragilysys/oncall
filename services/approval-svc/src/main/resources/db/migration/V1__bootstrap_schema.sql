-- ============================================================
-- approval-svc schema
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

CREATE INDEX IF NOT EXISTS idx_approval_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

-- Approval requests — unified queue for all resource types that require managerial approval.
-- reference_type: LEAVE_REQUEST | SWAP_REQUEST | MIDWEEK_REASSIGNMENT | EMERGENCY_OOO
-- Status flow: PENDING → APPROVED | REJECTED | DELEGATED
CREATE TABLE IF NOT EXISTS approval_requests (
    id               UUID         PRIMARY KEY,
    reference_type   VARCHAR(40)  NOT NULL,
    reference_id     UUID         NOT NULL,
    requestor_id     UUID         NOT NULL,
    approver_id      UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    decision_reason  TEXT,
    decided_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_approval_reference     ON approval_requests (reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_approval_approver_id   ON approval_requests (approver_id);
CREATE INDEX IF NOT EXISTS idx_approval_requestor_id  ON approval_requests (requestor_id);
CREATE INDEX IF NOT EXISTS idx_approval_status        ON approval_requests (status);

-- Approval delegations — a manager temporarily delegates approval authority to a colleague.
CREATE TABLE IF NOT EXISTS approval_delegations (
    id             UUID        PRIMARY KEY,
    delegator_id   UUID        NOT NULL,
    delegatee_id   UUID        NOT NULL,
    valid_from     TIMESTAMPTZ NOT NULL,
    valid_until    TIMESTAMPTZ NOT NULL,
    active         BOOLEAN     NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_delegation_delegator ON approval_delegations (delegator_id);
CREATE INDEX IF NOT EXISTS idx_delegation_delegatee ON approval_delegations (delegatee_id);
CREATE INDEX IF NOT EXISTS idx_delegation_active    ON approval_delegations (delegatee_id, active);

