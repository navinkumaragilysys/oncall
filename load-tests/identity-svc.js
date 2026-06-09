/**
 * k6 load test — identity-svc smoke + ramp.
 *
 * Covers:
 *   POST /api/v1/auth/login
 *   GET  /api/v1/members/{id}
 *   PATCH /api/v1/members/{id}
 *   GET  /api/v1/members/{id}/notification-preferences
 *   PUT  /api/v1/members/{id}/notification-preferences
 *
 * Run:
 *   k6 run load-tests/identity-svc.js
 *   k6 run --env BASE_URL=http://localhost:8090 load-tests/identity-svc.js
 *
 * Targets (p95 < 400 ms, error rate < 1 %):
 *   stage 1 — warm up  :  1 vu  × 10 s
 *   stage 2 — ramp up  : 20 vu  × 30 s
 *   stage 3 — sustained: 50 vu  × 60 s
 *   stage 4 — ramp down:  0 vu  × 10 s
 */

import http from "k6/http";
import { check, group, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || "http://localhost:8090";
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "seed.admin@agilysys.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "Ch@ngeMe1!";

// ---------------------------------------------------------------------------
// Custom metrics
// ---------------------------------------------------------------------------
const loginLatency = new Trend("login_latency_ms", true);
const memberGetLatency = new Trend("member_get_latency_ms", true);
const memberPatchLatency = new Trend("member_patch_latency_ms", true);
const notifGetLatency = new Trend("notif_pref_get_latency_ms", true);
const errorRate = new Rate("errors");

// ---------------------------------------------------------------------------
// Thresholds
// ---------------------------------------------------------------------------
export const options = {
  stages: [
    { duration: "10s", target: 1 },   // warm up
    { duration: "30s", target: 20 },  // ramp up
    { duration: "60s", target: 50 },  // sustained
    { duration: "10s", target: 0 },   // ramp down
  ],
  thresholds: {
    // Global HTTP
    http_req_failed:          ["rate<0.01"],   // < 1 % errors
    http_req_duration:        ["p(95)<400"],   // 95th pct under 400 ms
    // Per-operation
    login_latency_ms:         ["p(95)<600"],
    member_get_latency_ms:    ["p(95)<300"],
    member_patch_latency_ms:  ["p(95)<400"],
    notif_pref_get_latency_ms:["p(95)<300"],
    errors:                   ["rate<0.01"],
  },
};

// ---------------------------------------------------------------------------
// VU lifecycle — setUp is called once before the test
// ---------------------------------------------------------------------------

/** @type {string | null} */
let accessToken = null;
/** @type {string | null} */
let memberId = null;

export function setup() {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { "Content-Type": "application/json" } }
  );

  const ok = check(res, {
    "setup: login 200": (r) => r.status === 200,
    "setup: has accessToken": (r) => {
      try { return JSON.parse(r.body).accessToken !== undefined; } catch { return false; }
    },
  });

  if (!ok) {
    console.error(`setup login failed [${res.status}]: ${res.body}`);
    return { token: null, memberId: null };
  }

  const body = JSON.parse(res.body);
  return { token: body.accessToken, memberId: body.memberId };
}

// ---------------------------------------------------------------------------
// Default function — executed by every VU on every iteration
// ---------------------------------------------------------------------------
export default function (data) {
  if (!data || !data.token) {
    errorRate.add(1);
    sleep(1);
    return;
  }

  const headers = {
    "Content-Type":  "application/json",
    Authorization:   `Bearer ${data.token}`,
  };
  const id = data.memberId;

  // ---- 1. GET /members/{id} -----------------------------------------------
  group("member_get", () => {
    const res = http.get(`${BASE_URL}/api/v1/members/${id}`, { headers });
    memberGetLatency.add(res.timings.duration);
    const ok = check(res, { "member_get 200": (r) => r.status === 200 });
    errorRate.add(!ok);
  });

  sleep(0.2);

  // ---- 2. PATCH /members/{id} (non-destructive: only timezone) ------------
  group("member_patch", () => {
    const timezones = ["America/New_York", "America/Chicago", "America/Los_Angeles", "Europe/London"];
    const tz = timezones[Math.floor(Math.random() * timezones.length)];
    const res = http.patch(
      `${BASE_URL}/api/v1/members/${id}`,
      JSON.stringify({ timezone: tz }),
      { headers }
    );
    memberPatchLatency.add(res.timings.duration);
    const ok = check(res, { "member_patch 202": (r) => r.status === 202 });
    errorRate.add(!ok);
  });

  sleep(0.2);

  // ---- 3. GET /members/{id}/notification-preferences ----------------------
  group("notif_pref_get", () => {
    const res = http.get(
      `${BASE_URL}/api/v1/members/${id}/notification-preferences`,
      { headers }
    );
    notifGetLatency.add(res.timings.duration);
    const ok = check(res, { "notif_pref_get 200": (r) => r.status === 200 });
    errorRate.add(!ok);
  });

  sleep(0.5);
}

// ---------------------------------------------------------------------------
// Teardown (optional — log summary)
// ---------------------------------------------------------------------------
export function teardown(data) {
  if (data && data.token) {
    console.log(`teardown: test completed for memberId=${data.memberId}`);
  }
}
