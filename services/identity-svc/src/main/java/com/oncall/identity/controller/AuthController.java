package com.oncall.identity.controller;

import com.oncall.identity.dto.request.LoginRequest;
import com.oncall.identity.dto.request.RefreshTokenRequest;
import com.oncall.identity.dto.response.AuthResponse;
import com.oncall.identity.security.AuthenticatedMember;
import com.oncall.identity.security.JwtTokenProvider;
import com.oncall.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Public and authenticated authentication endpoints.
 *
 * Public (no token required):
 *   POST  /api/v1/auth/login
 *   POST  /api/v1/auth/refresh
 *   GET   /api/v1/auth/.well-known/jwks.json
 *
 * Authenticated (valid access token required):
 *   POST  /api/v1/auth/logout
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) throws Exception {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) throws Exception {
        return ResponseEntity.ok(authService.refresh(req));
    }

    /**
     * POST /api/v1/auth/logout
     * Requires a valid access token (Authorization: Bearer <token>).
     * Clears the server-side refresh token hash so the refresh token cannot be
     * used again.  The access token itself expires naturally (max 30 min).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedMember member) {
        authService.logout(member.memberId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Cache-Control", "public, max-age=3600")
                .body(tokenProvider.getJwksJson());
    }
}
