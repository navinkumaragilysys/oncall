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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${oncall.jwt.access-token-expiry-minutes:15}")
    private long accessTokenExpiryMinutes;

    @Transactional(readOnly = true)
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

        return buildAuthResponse(member);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest req) throws Exception {
        var jwt = tokenProvider.parseAndValidate(req.refreshToken());

        // Verify it is a refresh token
        String type = jwt.getJWTClaimsSet().getStringClaim("type");
        if (!"refresh".equals(type)) {
            throw new DomainException("Not a refresh token");
        }

        var memberId = tokenProvider.extractMemberId(jwt);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DomainException("Member not found"));

        if (member.getStatus() == MemberStatus.DEACTIVATED) {
            throw new DomainException("Account is deactivated");
        }

        return buildAuthResponse(member);
    }

    private AuthResponse buildAuthResponse(Member member) throws Exception {
        List<String> roles = member.getSystemRoles().stream().map(Enum::name).toList();
        String accessToken = tokenProvider.issueAccessToken(member.getId(), member.getEmail(), roles);
        String refreshToken = tokenProvider.issueRefreshToken(member.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                accessTokenExpiryMinutes * 60,
                member.getId(),
                member.getEmail(),
                roles
        );
    }
}
