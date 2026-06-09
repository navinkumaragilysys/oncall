-- V1: Identity service schema
-- Manages: members, member credentials, team memberships, notification preferences,
--          outbox events (CDC relay to oncall.identity.events Kafka topic)

-- ── Members ──────────────────────────────────────────────────────────────────
CREATE TABLE members (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name       VARCHAR(150)    NOT NULL,
    display_name    VARCHAR(100),
    email           VARCHAR(255)    NOT NULL,
    region          VARCHAR(20)     NOT NULL,
    timezone        VARCHAR(60)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    oncall_eligible BOOLEAN         NOT NULL DEFAULT TRUE,
    join_date       DATE,
    slack_handle    VARCHAR(100),
    phone           VARCHAR(50),
    manager_id      UUID            REFERENCES members(id),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT members_email_unique UNIQUE (email),
    CONSTRAINT members_status_check CHECK (status IN ('ACTIVE','ON_LEAVE','DEACTIVATED')),
    CONSTRAINT members_region_check CHECK (region IN ('US_PST','US_EST','IDC_IST'))
);

CREATE INDEX idx_members_email   ON members (lower(email));
CREATE INDEX idx_members_status  ON members (status);
CREATE INDEX idx_members_manager ON members (manager_id);

-- ── Member system roles (ElementCollection) ──────────────────────────────────
CREATE TABLE member_system_roles (
    member_id   UUID        NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    system_role VARCHAR(30) NOT NULL,
    PRIMARY KEY (member_id, system_role),
    CONSTRAINT member_system_roles_role_check CHECK (
        system_role IN ('ROLE_ADMIN','ROLE_MANAGER','ROLE_ONCALL_HOST','ROLE_MEMBER')
    )
);

-- ── Member credentials (passwords stored separately from identity data) ───────
CREATE TABLE member_credentials (
    member_id           UUID        PRIMARY KEY REFERENCES members(id) ON DELETE CASCADE,
    password_hash       VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Team memberships ──────────────────────────────────────────────────────────
-- Note: team_id references teams table managed by team-policy-svc.
-- FK is intentionally omitted here (cross-service reference — enforced at app level).
CREATE TABLE team_memberships (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id   UUID        NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    team_id     UUID        NOT NULL,
    team_role   VARCHAR(30),
    joined_on   DATE,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT team_memberships_uc UNIQUE (member_id, team_id),
    CONSTRAINT team_memberships_role_check CHECK (
        team_role IS NULL OR team_role IN ('ROLE_ADMIN','ROLE_MANAGER','ROLE_ONCALL_HOST','ROLE_MEMBER')
    )
);

CREATE INDEX idx_team_memberships_member ON team_memberships (member_id);
CREATE INDEX idx_team_memberships_team   ON team_memberships (team_id);

-- ── Notification preferences ──────────────────────────────────────────────────
CREATE TABLE notification_preferences (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id   UUID        NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    channel     VARCHAR(20) NOT NULL,
    event_type  VARCHAR(60) NOT NULL,
    enabled     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT notification_preferences_uc UNIQUE (member_id, channel, event_type)
);

CREATE INDEX idx_notif_prefs_member ON notification_preferences (member_id);

-- ── Outbox events (CDC relay — Debezium reads this table) ────────────────────
CREATE TABLE outbox_events (
    id             UUID         PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    topic          VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ,
    retry_count    INT          NOT NULL DEFAULT 0
);

-- Partial index: only unpublished rows — keeps the poller fast
CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at)
    WHERE published = FALSE;

-- ── updated_at trigger ────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER members_updated_at
    BEFORE UPDATE ON members
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER team_memberships_updated_at
    BEFORE UPDATE ON team_memberships
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER notification_preferences_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
