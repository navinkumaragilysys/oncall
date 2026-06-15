-- V1: Team policy service schema
-- Owns teams, rotation policies, sub-session definitions, shift definitions,
-- and holiday calendars. Includes outbox table for async CUD event publishing.

CREATE TABLE IF NOT EXISTS rotation_policies (
    id                                  UUID PRIMARY KEY,
    name                                VARCHAR(150) NOT NULL UNIQUE,
    strategy                            VARCHAR(50)  NOT NULL,
    rotation_length_days                INTEGER      NOT NULL,
    min_gap_days                        INTEGER      NOT NULL,
    weekend_policy_separate             BOOLEAN      NOT NULL,
    secondary_enabled                   BOOLEAN      NOT NULL,
    secondary_fairness_weight           DOUBLE PRECISION NOT NULL,
    same_team_primary_secondary_allowed BOOLEAN      NOT NULL,
    horizon_months                      INTEGER      NOT NULL,
    allow_multi_subsession_per_week     BOOLEAN      NOT NULL,
    created_at                          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS teams (
    id                      UUID PRIMARY KEY,
    name                    VARCHAR(150) NOT NULL UNIQUE,
    region                  VARCHAR(30)  NOT NULL,
    policy_id               UUID REFERENCES rotation_policies(id),
    min_eligible_threshold  INTEGER      NOT NULL,
    active                  BOOLEAN      NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_teams_region ON teams(region);
CREATE INDEX IF NOT EXISTS idx_teams_policy ON teams(policy_id);

CREATE TABLE IF NOT EXISTS sub_session_definitions (
    id                  UUID PRIMARY KEY,
    policy_id           UUID        NOT NULL REFERENCES rotation_policies(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    offset_start_days   INTEGER      NOT NULL,
    offset_start_time   TIME         NOT NULL,
    offset_end_days     INTEGER      NOT NULL,
    offset_end_time     TIME         NOT NULL,
    ordinal             INTEGER      NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_sub_session_policy_ordinal UNIQUE (policy_id, ordinal)
);

CREATE INDEX IF NOT EXISTS idx_sub_sessions_policy ON sub_session_definitions(policy_id);

CREATE TABLE IF NOT EXISTS shift_definitions (
    id                   UUID PRIMARY KEY,
    shift_type           VARCHAR(20) NOT NULL,
    region               VARCHAR(30) NOT NULL,
    dst_aware            BOOLEAN     NOT NULL,
    standard_start_day   VARCHAR(20),
    standard_start_time  TIME        NOT NULL,
    standard_end_day     VARCHAR(20),
    standard_end_time    TIME        NOT NULL,
    dst_start_day        VARCHAR(20),
    dst_start_time       TIME,
    dst_end_day          VARCHAR(20),
    dst_end_time         TIME,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_shift_region_type ON shift_definitions(region, shift_type);

CREATE TABLE IF NOT EXISTS holiday_calendars (
    id            UUID PRIMARY KEY,
    date          DATE        NOT NULL,
    name          VARCHAR(150) NOT NULL,
    region        VARCHAR(30) NOT NULL,
    type          VARCHAR(40) NOT NULL,
    shift_impact  VARCHAR(40) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_holiday_name_date_region UNIQUE (date, region, name)
);

CREATE INDEX IF NOT EXISTS idx_holiday_region_date ON holiday_calendars(region, date);

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

CREATE INDEX IF NOT EXISTS idx_team_policy_outbox_unpublished
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;
