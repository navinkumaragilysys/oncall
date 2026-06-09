package com.oncall.identity.config;

import com.oncall.domain.entity.Member;
import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.Region;
import com.oncall.domain.enums.SystemRole;
import com.oncall.identity.repository.MemberRepository;
import com.oncall.identity.security.MemberCredential;
import com.oncall.identity.security.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Seeds a default admin member on first startup in non-production environments.
 *
 * <p>Credentials: seed.admin@agilysys.com / Ch@ngeMe1!
 * These are used by k6 smoke/load tests and local API exploration tools.
 *
 * <p>This bean is excluded from the {@code prod} profile.  In production,
 * seed data must be managed via a controlled migration or admin console.
 */
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    static final String SEED_EMAIL    = "seed.admin@agilysys.com";
    static final String SEED_PASSWORD = "Ch@ngeMe1!";

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.existsByEmailIgnoreCase(SEED_EMAIL)) {
            log.debug("Seed admin already present — skipping DevDataSeeder");
            return;
        }

        Member admin = Member.builder()
                .fullName("Seed Admin")
                .displayName("admin")
                .email(SEED_EMAIL)
                .region(Region.US_EST)
                .timezone("America/New_York")
                .status(MemberStatus.ACTIVE)
                .oncallEligible(true)
                .joinDate(LocalDate.now())
                .systemRoles(Set.of(SystemRole.ROLE_ADMIN, SystemRole.ROLE_MEMBER))
                .build();
        admin = memberRepository.save(admin);

        credentialRepository.save(MemberCredential.builder()
                .memberId(admin.getId())
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .passwordChangedAt(Instant.now())
                .build());

        log.info("DevDataSeeder: created seed admin [{}]", SEED_EMAIL);
    }
}
