#!/usr/bin/env bash
# k6 integration test runner.
#
# Always deploys a FRESH Docker stack before running tests — no services run
# outside of Docker.  Tears everything down when done (unless --keep-running).
#
# Usage:
#   scripts/k6.sh [smoke|load] [service|all] [options]
#
# Modes:
#   smoke   — 1 VU × 1 iteration; every check must pass  (default)
#   load    — staged ramp test with threshold assertions
#
# Options:
#   --skip-deploy   Skip docker compose lifecycle (use an already-running stack)
#   --keep-running  Do NOT tear down the stack after tests finish
#   --no-cache      Pass --no-cache to docker compose build
#
# Environment:
#   GATEWAY_PORT     host port the gateway is mapped to  (default 8090)
#   BASE_URL         overrides http://localhost:$GATEWAY_PORT
#   LOGIN_EMAIL      (default seed.admin@agilysys.com)
#   LOGIN_PASSWORD   (default Ch@ngeMe1!)
#   K6_OUT           optional k6 --out argument  (e.g. json=results.json)
#   COMPOSE_FILE     (default docker-compose.yml)
#
# Examples:
#   scripts/k6.sh smoke identity-svc
#   scripts/k6.sh load  identity-svc --keep-running
#   BASE_URL=http://staging:8090 scripts/k6.sh smoke --skip-deploy

set -uo pipefail
cd "$(dirname "$0")/.."

# ── parse args ─────────────────────────────────────────────────────────────
MODE="smoke"
SERVICE="all"
SKIP_DEPLOY=false
KEEP_RUNNING=false
BUILD_CACHE_FLAG=""

for arg in "$@"; do
  case "$arg" in
    smoke|load)     MODE="$arg" ;;
    --skip-deploy)  SKIP_DEPLOY=true ;;
    --keep-running) KEEP_RUNNING=true ;;
    --no-cache)     BUILD_CACHE_FLAG="--no-cache" ;;
    -*)             echo "Unknown option: $arg" >&2; exit 2 ;;
    *)              SERVICE="$arg" ;;
  esac
done

# ── configuration ──────────────────────────────────────────────────────────
GATEWAY_PORT="${GATEWAY_PORT:-8090}"
BASE_URL="${BASE_URL:-http://localhost:${GATEWAY_PORT}}"
export BASE_URL
export LOGIN_EMAIL="${LOGIN_EMAIL:-seed.admin@agilysys.com}"
export LOGIN_PASSWORD="${LOGIN_PASSWORD:-Ch@ngeMe1!}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

# Services we have local builds for
JAVA_SERVICES="identity-svc gateway"
# Core infra services (DB, messaging, discovery, cache)
INFRA_SERVICES="postgres kafka consul redis"
# Observability stack — always running during tests
OBS_SERVICES="zipkin otel-collector prometheus grafana node-exporter redis-exporter kafka-ui"

# ── test registry ──────────────────────────────────────────────────────────
declare -A SMOKE_SCRIPTS=(
  [identity-svc]="load-tests/smoke/identity-svc-smoke.js"
)
declare -A LOAD_SCRIPTS=(
  [identity-svc]="load-tests/identity-svc.js"
)

# ── helpers ────────────────────────────────────────────────────────────────
log() { echo "  $*"; }
die() { echo "❌  $*" >&2; exit 1; }
sep() { echo; printf '%.0s━' {1..66}; echo; }

require_bin() {
  command -v "$1" &>/dev/null || die "$1 not found. $2"
}

# Poll docker inspect for container health status
wait_container_healthy() {
  local container="$1"
  local timeout="${2:-120}"
  local deadline=$(( $(date +%s) + timeout ))
  printf "    %-30s" "${container}..."
  while (( $(date +%s) < deadline )); do
    local status
    status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
    case "$status" in
      healthy) echo " ✓"; return 0 ;;
      missing) echo " ✗ (container not found)"; return 1 ;;
    esac
    printf "."; sleep 3
  done
  echo " ✗ TIMEOUT after ${timeout}s"
  docker compose -f "$COMPOSE_FILE" logs --tail=30 "${container#oncall-}" 2>/dev/null || true
  return 1
}

# Poll an HTTP endpoint until it returns 2xx
wait_http() {
  local url="$1"
  local timeout="${2:-60}"
  local deadline=$(( $(date +%s) + timeout ))
  printf "    %-50s" "${url}..."
  while (( $(date +%s) < deadline )); do
    if curl -sf --max-time 4 "$url" >/dev/null 2>&1; then
      echo " ✓"; return 0
    fi
    printf "."; sleep 3
  done
  echo " ✗ TIMEOUT"
  return 1
}

# ── deploy ─────────────────────────────────────────────────────────────────
deploy() {
  sep
  echo "🐳  Fresh Docker deployment"
  sep

  # 1. Tear down previous stack (remove volumes → clean DB state for Flyway)
  log "Tearing down previous stack + volumes..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true

  # 2. Build only the Java services we own (gateway + identity-svc)
  log "Building Java services: ${JAVA_SERVICES}..."
  # shellcheck disable=SC2086
  docker compose -f "$COMPOSE_FILE" build $BUILD_CACHE_FLAG $JAVA_SERVICES \
    || die "docker compose build failed"

  # 3. Start infra without dependency resolution (we own the order)
  log "Starting infra services: ${INFRA_SERVICES}..."
  # shellcheck disable=SC2086
  docker compose -f "$COMPOSE_FILE" up --no-deps -d $INFRA_SERVICES \
    || die "Failed to start infra services"

  sep
  log "Waiting for infra health..."
  wait_container_healthy "oncall-postgres" 90  || die "postgres failed to become healthy"
  wait_container_healthy "oncall-kafka"    120 || die "kafka failed to become healthy"
  wait_container_healthy "oncall-consul"   60  || die "consul failed to become healthy"
  wait_container_healthy "oncall-redis"    30  || die "redis failed to become healthy"

  # 4. Start observability stack (no hard deps on app services; start in parallel with app)
  log "Starting observability stack: ${OBS_SERVICES}..."
  # shellcheck disable=SC2086
  docker compose -f "$COMPOSE_FILE" up --no-deps -d $OBS_SERVICES \
    || die "Failed to start observability services"

  # Wait only for the services that expose healthchecks
  sep
  log "Waiting for observability health..."
  wait_container_healthy "oncall-zipkin"     60 || log "  zipkin: no healthcheck, continuing"
  wait_container_healthy "oncall-prometheus" 60 || log "  prometheus: timed out, continuing"

  # 5. Start application services without pulling unbuilt service images
  log "Starting app services: ${JAVA_SERVICES}..."
  # shellcheck disable=SC2086
  docker compose -f "$COMPOSE_FILE" up --no-deps -d $JAVA_SERVICES \
    || die "Failed to start app services"

  sep
  log "Waiting for app health..."
  wait_container_healthy "oncall-identity" 120 || die "identity-svc failed health check"
  wait_container_healthy "oncall-gateway"  90  || die "gateway failed health check"

  # 6. Verify end-to-end routing: gateway → identity-svc JWKS endpoint
  log "Verifying end-to-end route (gateway → identity-svc)..."
  wait_http "${BASE_URL}/health/live" 30 \
    || die "Gateway health endpoint not responding"
  wait_http "${BASE_URL}/api/v1/auth/.well-known/jwks.json" 60 \
    || die "JWKS route via gateway not responding — identity-svc may not be registered in Consul"

  sep
  echo "✅  Stack is ready at ${BASE_URL}"
  sep
}

# ── cleanup ─────────────────────────────────────────────────────────────────
cleanup() {
  if [[ "$KEEP_RUNNING" == "true" ]]; then
    log "Stack left running (--keep-running)"
    return
  fi
  sep
  log "Tearing down stack..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true
  echo "✅  Stack torn down"
}

# ── k6 runner ───────────────────────────────────────────────────────────────
K6_EXTRA_ARGS=()
[[ -n "${K6_OUT:-}" ]] && K6_EXTRA_ARGS+=(--out "$K6_OUT")

run_k6() {
  local script="$1"
  local label="$2"
  sep
  echo "  k6 ${MODE} → ${label}  [${BASE_URL}]"
  sep
  k6 run "${K6_EXTRA_ARGS[@]}" "$script"
}

FAILED=0

run_service() {
  local svc="$1"
  if [[ "$MODE" == "smoke" ]]; then
    local script="${SMOKE_SCRIPTS[$svc]:-}"
    [[ -z "$script" ]] && { echo "No smoke script registered for '$svc'" >&2; return 1; }
    run_k6 "$script" "$svc" || FAILED=1
  elif [[ "$MODE" == "load" ]]; then
    local script="${LOAD_SCRIPTS[$svc]:-}"
    [[ -z "$script" ]] && { echo "No load script registered for '$svc'" >&2; return 1; }
    run_k6 "$script" "$svc" || FAILED=1
  else
    die "Unknown mode '${MODE}'. Use: smoke | load"
  fi
}

# ── main ────────────────────────────────────────────────────────────────────
require_bin k6     "Install from https://k6.io/docs/get-started/installation/"
require_bin docker "Install Docker from https://docs.docker.com/get-docker/"

[[ "$SKIP_DEPLOY" == "false" ]] && deploy

# ── Always pre-warm the gateway JWKS cache before running tests ──────────────
# NimbusReactiveJwtDecoder fetches JWKS lazily on the first JWT-validated
# request.  When the stack is freshly started (or restarted) the JWKS cache
# is cold.  Pre-warming here ensures k6 never hits the cold-cache race
# regardless of whether --skip-deploy was used.
sep
log "Pre-warming gateway JWKS cache..."
WARM_LOGIN=$(curl -sf -X POST "${BASE_URL}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"seed.admin@agilysys.com","password":"Ch@ngeMe1!"}' 2>/dev/null) \
  || die "Pre-warm login failed — is the stack running?"
WARM_TOKEN=$(echo "$WARM_LOGIN" | python3 -c "import json,sys; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null) \
  || die "Pre-warm: could not parse accessToken"
WARM_MEMBER=$(echo "$WARM_LOGIN" | python3 -c "import json,sys; print(json.load(sys.stdin)['memberId'])" 2>/dev/null)
warmed=false
for i in $(seq 1 20); do
  warm_status=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${WARM_TOKEN}" \
    "${BASE_URL}/api/v1/members/${WARM_MEMBER}" 2>/dev/null)
  if [[ "$warm_status" == "200" ]]; then
    warmed=true
    log "  JWKS cache warm (attempt ${i})"
    break
  fi
  sleep 3
done
[[ "$warmed" == "true" ]] || die "Gateway JWKS cache did not warm after 60 s — check gateway logs"
sep

# Register cleanup even if tests fail
trap cleanup EXIT

if [[ "$SERVICE" == "all" ]]; then
  if [[ "$MODE" == "smoke" ]]; then
    for svc in "${!SMOKE_SCRIPTS[@]}"; do run_service "$svc"; done
  else
    for svc in "${!LOAD_SCRIPTS[@]}"; do run_service "$svc"; done
  fi
else
  run_service "$SERVICE"
fi

echo
if (( FAILED )); then
  echo "❌  One or more k6 runs FAILED." >&2
  exit 1
fi
echo "✅  All k6 runs passed."
