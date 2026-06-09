package com.oncall.identity.dto.response;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UUID memberId,
        String email,
        List<String> roles
) {}
