package com.oncall.identity.service;

import com.oncall.domain.entity.Member;
import com.oncall.domain.enums.MemberStatus;
import com.oncall.identity.dto.request.LoginRequest;
import com.oncall.identity.dto.request.RefreshTokenRequest;
import com.oncall.identity.dto.response.AuthResponse;
import com.oncall.identity.exception.DomainException;
import com.oncall.identity.repository.MemberRepository;
import com.oncall.identity.security.JwtTokenProvider;
import com.oncall.identity.security.MemberCredential;
import com.oncall.identity.security.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${oncall.jwt.access-token-expiry-minutes:30}")
    private long accessTokenExpiryMinutes;

    // ──────────────────────────────────────────────────────────────────────
    // Login — validates credentials and issues a token pair
    // ──────────────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest req) throws Exception {
        Member member = memberRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new DomainException("Invalid credentials"));

        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new DomainException("Account is deactivated. Contact your administrator.");
        }

        MemberCredential credential = credentialRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new DomainException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), credential.getPasswordHash())) {
            throw new DomainException("Invalid credentials");
        }

        return issueTokenPair(member, credential);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Refresh — rotates the refresh token; detects reuse attacks
    // ──────────────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) throws Exception {
        var jwt = tokenProvider.parseAndValidate(req.refreshToken());

        String type = jwt.getJWTClaimsSet().getStringClaim("type");
        if (!"refresh".equals(type)) {
            throw new DomainException("Not a refresh token");
        }

        UUID memberId = tokenProvider.extractMemberId(jwt);
        MemberCredential credential = credentialRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DomainException("Invalid refresh token"));

        // Token reuse detection: if hash doesn't match, the token was already
        // rotated or revoked — clear the stored token and force re-login.
        String incomingHash = sha256(req.refreshToken());
        if (!incomingHash.equals(credential.getRefreshTokenHash())) {
            credential.setRefreshTokenHash(null);
            credential.setRefreshTokenExpiresAt(null);
            credentialRepository.save(credential);
            log.warn("Refresh token reuse detected for memberId={}. Invalidating all sessions.", memberId);
            throw new DomainException("Refresh token already used or revoked. Please log in again.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DomainException("Member not found"));

        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new DomainException("Account is deactivated");
        }

        return issueTokenPair(member, credential);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Logout — invalidates the stored refresh token hash
    // ──────────────────────────────────────────────────────────────────────
    @Transactional
    public void logout(UUID memberId) {
        credentialRepository.findByMemberId(memberId).ifPresent(cred -> {
            cred.setRefreshTokenHash(null);
            cred.setRefreshTokenExpiresAt(null);
            credentialRepository.save(cred);
            log.info("Refresh token invalidated for memberId={}", memberId);
        });
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Issues a new access + refresh token pair, rotates the stored hash,
     * and returns the full AuthResponse.
     */
    private AuthResponse issueTokenPair(Member member, MemberCredential credential) throws Exception {
        List<String> roles = member.getSystemRoles().stream().map(Enum::name).toList();
        String accessToken  = tokenProvider.issueAccessToken(member.getId(), member.getEmail(), roles);
        String refreshToken = tokenProvider.issueRefreshToken(member.getId());

        // Rotate: store the SHA-256 hash of the new refresh token
        var refreshJwt = tokenProvider.parseAndValidate(refreshToken);
        Instant refreshExpiry = refreshJwt.getJWTClaimsSet().getExpirationTime().toInstant();
        credential.setRefreshTokenHash(sha256(refreshToken));
        credential.setRefreshTokenExpiresAt(refreshExpiry);
        credentialRepository.save(credential);

        return new AuthResponse(
                accessToken,
                refreshToken,
                accessTokenExpiryMinutes * 60,
                member.getId(),
                member.getEmail(),
                roles
        );
    }

    /** SHA-256 hex digest — used to store refresh tokens without exposing the raw value. */
    static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
