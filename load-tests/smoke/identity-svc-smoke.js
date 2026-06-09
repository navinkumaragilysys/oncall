/**
 * k6 smoke test — identity-svc.
 *
 * A single VU that fires each critical endpoint once and asserts correctness.
 * Intended for CI: fast (< 30 s), zero sustained load.
 *
 * Run:
 *   k6 run load-tests/smoke/identity-svc-smoke.js
 *   k6 run --env BASE_URL=http://localhost:8090 load-tests/smoke/identity-svc-smoke.js
 *
 * Exit code 0 = all checks pass; non-zero = at least one check failed.
 */

import http from "k6/http";
import { check, group, fail } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8090";
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "seed.admin@agilysys.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "Ch@ngeMe1!";

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    checks: ["rate==1.00"],  // every single check must pass
  },
};

export default function () {
  // ------------------------------------------------------------------
  // 1. Login
  // ------------------------------------------------------------------
  let token, memberId, refreshToken;

  group("auth_login", () => {
    const res = http.post(
      `${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
      { headers: { "Content-Type": "application/json" } }
    );

    const ok = check(res, {
      "login: status 200":     (r) => r.status === 200,
      "login: accessToken":    (r) => { try { return !!JSON.parse(r.body).accessToken; } catch { return false; } },
      "login: refreshToken":   (r) => { try { return !!JSON.parse(r.body).refreshToken; } catch { return false; } },
      "login: memberId":       (r) => { try { return !!JSON.parse(r.body).memberId; } catch { return false; } },
    });

    if (!ok) fail(`login failed [${res.status}]: ${res.body}`);

    const body = JSON.parse(res.body);
    token = body.accessToken;
    refreshToken = body.refreshToken;
    memberId = body.memberId;
  });

  const headers = {
    "Content-Type": "application/json",
    Authorization:  `Bearer ${token}`,
  };

  // ------------------------------------------------------------------
  // 2. Token refresh
  // ------------------------------------------------------------------
  group("auth_refresh", () => {
    const res = http.post(
      `${BASE_URL}/api/v1/auth/refresh`,
      JSON.stringify({ refreshToken }),
      { headers: { "Content-Type": "application/json" } }
    );
    check(res, {
      "refresh: status 200":  (r) => r.status === 200,
      "refresh: accessToken": (r) => { try { return !!JSON.parse(r.body).accessToken; } catch { return false; } },
    });
  });

  // ------------------------------------------------------------------
  // Gateway JWKS warmup — the reactive Netty JWKS cache is cold on the
  // first JWT-authenticated request.  Retry until the gateway validates
  // the token (max ~12 s) before running the real assertion groups.
  // ------------------------------------------------------------------
  for (let i = 0; i < 6; i++) {
    const warmRes = http.get(`${BASE_URL}/api/v1/members/${memberId}`, { headers });
    if (warmRes.status === 200) break;
    sleep(2);
  }

  // ------------------------------------------------------------------
  // 3. GET /members/{id}
  // ------------------------------------------------------------------
  group("member_get_self", () => {
    const res = http.get(`${BASE_URL}/api/v1/members/${memberId}`, { headers });
    check(res, {
      "member_get: status 200": (r) => r.status === 200,
      "member_get: has email":  (r) => { try { return !!JSON.parse(r.body).email; } catch { return false; } },
    });
  });

  // ------------------------------------------------------------------
  // 4. GET /members  (list, admin only)
  // ------------------------------------------------------------------
  group("member_list", () => {
    const res = http.get(`${BASE_URL}/api/v1/members`, { headers });
    check(res, {
      "member_list: status 200 or 403": (r) => r.status === 200 || r.status === 403,
    });
  });

  // ------------------------------------------------------------------
  // 5. PATCH /members/{id}  (safe field only)
  // ------------------------------------------------------------------
  group("member_patch", () => {
    const res = http.patch(
      `${BASE_URL}/api/v1/members/${memberId}`,
      JSON.stringify({ timezone: "UTC" }),
      { headers }
    );
    check(res, {
      "member_patch: status 202": (r) => r.status === 202,
    });
  });

  // ------------------------------------------------------------------
  // 6. GET /members/{id}/notification-preferences
  // ------------------------------------------------------------------
  group("notif_pref_get", () => {
    const res = http.get(
      `${BASE_URL}/api/v1/members/${memberId}/notification-preferences`,
      { headers }
    );
    check(res, {
      "notif_pref_get: status 200": (r) => r.status === 200,
    });
  });

  // ------------------------------------------------------------------
  // 7. PUT /members/{id}/notification-preferences
  // ------------------------------------------------------------------
  group("notif_pref_put", () => {
    const res = http.put(
      `${BASE_URL}/api/v1/members/${memberId}/notification-preferences`,
      JSON.stringify({
        channel: "EMAIL",
        eventType: "SCHEDULE_PUBLISHED",
        enabled: true,
      }),
      { headers }
    );
    check(res, {
      "notif_pref_put: status 200 or 202": (r) => r.status === 200 || r.status === 202,
    });
  });

  // ------------------------------------------------------------------
  // 8. Unauthenticated request is rejected
  // ------------------------------------------------------------------
  group("auth_required", () => {
    const res = http.get(`${BASE_URL}/api/v1/members/${memberId}`);
    check(res, {
      "no-token: status 401": (r) => r.status === 401,
    });
  });
}
