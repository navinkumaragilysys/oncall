-- On-Call Platform — PostgreSQL database initialisation
-- Runs once on first container boot (docker-entrypoint-initdb.d).
-- Creates one isolated database per microservice (database-per-service pattern).
-- The superuser role (${POSTGRES_USER}) owns all databases.

CREATE DATABASE oncall_identity;
CREATE DATABASE oncall_team;
CREATE DATABASE oncall_schedule;
CREATE DATABASE oncall_assignment;
CREATE DATABASE oncall_worklog;
CREATE DATABASE oncall_ticket;
CREATE DATABASE oncall_handover;
CREATE DATABASE oncall_availability;
CREATE DATABASE oncall_approval;
CREATE DATABASE oncall_notification;
CREATE DATABASE oncall_audit;
CREATE DATABASE oncall_reporting;

-- Grant full privileges on each service database to the application role.
GRANT ALL PRIVILEGES ON DATABASE oncall_identity    TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_team        TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_schedule    TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_assignment  TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_worklog     TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_ticket      TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_handover    TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_availability TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_approval    TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_notification TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_audit       TO oncall;
GRANT ALL PRIVILEGES ON DATABASE oncall_reporting   TO oncall;
