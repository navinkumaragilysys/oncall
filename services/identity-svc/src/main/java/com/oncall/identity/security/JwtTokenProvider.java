package com.oncall.identity.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Issues and validates RS256 JWTs.
 *
 * Key lifecycle:
 * - Dev: RSA key pair generated in-memory on startup (not suitable for multi-instance prod).
 * - Prod: set {@code oncall.jwt.private-key-pem} and {@code oncall.jwt.public-key-pem} env vars
 *   (or mount a secret); the key is loaded from PEM.
 *
 * The JWKS (public key set) is exposed at {@code /api/v1/auth/.well-known/jwks.json}
 * so the gateway can validate tokens without calling identity-svc on every request.
 *
 * JWT claims:
 * - sub       = memberId (UUID string)
 * - email     = member email
 * - roles     = List<String> of SystemRole names  (e.g. ["ROLE_ADMIN"])
 * - jti       = unique token ID (for revocation if needed)
 * - iss       = "oncall-identity"
 * - iat / exp = issued-at / expiry
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${oncall.jwt.access-token-expiry-minutes:15}")
    private long accessTokenExpiryMinutes;

    @Value("${oncall.jwt.refresh-token-expiry-days:7}")
    private long refreshTokenExpiryDays;

    private RSAKey rsaKey;
    private RSASSASigner signer;
    private RSASSAVerifier verifier;

    @Getter
    private String jwksJson;

    @PostConstruct
    public void init() throws Exception {
        // In production, load from environment/secret — here we generate for dev convenience
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("oncall-identity-key-1")
                .generate();

        signer = new RSASSASigner(rsaKey);
        verifier = new RSASSAVerifier(rsaKey.toPublicJWK());
        jwksJson = new JWKSet(rsaKey.toPublicJWK()).toString();
        log.info("JWT RSA key pair initialised (keyId=oncall-identity-key-1)");
    }

    /**
     * Issues a short-lived access token (15 min default).
     */
    public String issueAccessToken(UUID memberId, String email, List<String> roles) throws Exception {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMinutes * 60 * 1000L);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(memberId.toString())
                .issuer("oncall-identity")
                .issueTime(now)
                .expirationTime(expiry)
                .jwtID(UUID.randomUUID().toString())
                .claim("email", email)
                .claim("roles", roles)
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        jwt.sign(signer);
        return jwt.serialize();
    }

    /**
     * Issues a long-lived refresh token (7 days default).
     * Refresh tokens are stored (hashed) in the DB; this method issues a raw token.
     */
    public String issueRefreshToken(UUID memberId) throws Exception {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiryDays * 24 * 60 * 60 * 1000L);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(memberId.toString())
                .issuer("oncall-identity")
                .issueTime(now)
                .expirationTime(expiry)
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        jwt.sign(signer);
        return jwt.serialize();
    }

    /**
     * Parses and validates a JWT string. Returns the parsed {@link SignedJWT}.
     * Throws if the token is invalid, expired, or has a bad signature.
     */
    public SignedJWT parseAndValidate(String tokenString) throws Exception {
        SignedJWT jwt = SignedJWT.parse(tokenString);
        if (!jwt.verify(verifier)) {
            throw new SecurityException("JWT signature verification failed");
        }
        Date expiry = jwt.getJWTClaimsSet().getExpirationTime();
        if (expiry == null || expiry.before(new Date())) {
            throw new SecurityException("JWT token has expired");
        }
        return jwt;
    }

    /**
     * Extracts the memberId (sub claim) from a pre-validated JWT.
     */
    public UUID extractMemberId(SignedJWT jwt) throws Exception {
        return UUID.fromString(jwt.getJWTClaimsSet().getSubject());
    }

    /**
     * Extracts roles list from claims.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(SignedJWT jwt) throws Exception {
        Object roles = jwt.getJWTClaimsSet().getClaim("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return List.of();
    }
}
