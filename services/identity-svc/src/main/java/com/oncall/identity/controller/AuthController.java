package com.oncall.identity.controller;

import com.oncall.identity.dto.request.LoginRequest;
import com.oncall.identity.dto.request.RefreshTokenRequest;
import com.oncall.identity.dto.response.AuthResponse;
import com.oncall.identity.security.JwtTokenProvider;
import com.oncall.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public authentication endpoints.
 * All paths under /api/v1/auth/** are permit-all in SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    /**
     * POST /api/v1/auth/login
     * Body: { "email": "user@agilysys.com", "password": "..." }
     * Returns: access token + refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) throws Exception {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * POST /api/v1/auth/refresh
     * Body: { "refreshToken": "..." }
     * Returns: new access token + new refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) throws Exception {
        return ResponseEntity.ok(authService.refresh(req));
    }

    /**
     * GET /api/v1/auth/.well-known/jwks.json
     * Returns the RSA public key set used by the gateway to validate tokens.
     * Response is plain JSON — no Content-Type override needed (Spring handles it).
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Cache-Control", "public, max-age=3600")
                .body(tokenProvider.getJwksJson());
    }
}
