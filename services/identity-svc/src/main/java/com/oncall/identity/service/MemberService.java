package com.oncall.identity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncall.domain.entity.Member;
import com.oncall.domain.entity.NotificationPreference;
import com.oncall.domain.entity.TeamMembership;
import com.oncall.domain.enums.MemberStatus;
import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import com.oncall.domain.enums.SystemRole;
import com.oncall.identity.dto.request.CreateMemberRequest;
import com.oncall.identity.dto.request.UpdateMemberRequest;
import com.oncall.identity.dto.response.MemberResponse;
import com.oncall.identity.dto.response.NotificationPreferenceResponse;
import com.oncall.identity.exception.DomainException;
import com.oncall.identity.exception.ResourceNotFoundException;
import com.oncall.identity.mapper.IdentityMapper;
import com.oncall.identity.outbox.OutboxPublisher;
import com.oncall.identity.repository.MemberRepository;
import com.oncall.identity.repository.NotificationPreferenceRepository;
import com.oncall.identity.security.AuthenticatedMember;
import com.oncall.identity.security.MemberCredential;
import com.oncall.identity.security.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String TOPIC = "oncall.identity.events";
    private static final Set<NotificationEventType> NON_DISABLEABLE = EnumSet.of(
            NotificationEventType.EMERGENCY_OOO_DECLARED,
            NotificationEventType.EMERGENCY_REPLACEMENT_SELECTED,
            NotificationEventType.REMINDER_T24H
    );

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final NotificationPreferenceRepository notifPrefRepository;
    private final OutboxPublisher outboxPublisher;
    private final IdentityMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ── READ ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MemberResponse getById(UUID memberId) {
        return mapper.toMemberResponse(findOrThrow(memberId));
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> listMembers(MemberStatus status, Pageable pageable) {
        Page<Member> page = (status != null)
                ? memberRepository.findByStatus(status, pageable)
                : memberRepository.findAll(pageable);
        return page.map(mapper::toMemberResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getNotificationPreferences(UUID memberId) {
        return notifPrefRepository.findByMemberId(memberId)
                .stream().map(mapper::toNotificationPreferenceResponse).toList();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    @SneakyThrows
    public MemberResponse createMember(CreateMemberRequest req) {
        if (memberRepository.existsByEmailIgnoreCase(req.email())) {
            throw new DomainException("Email already registered: " + req.email());
        }

        Member manager = (req.managerId() != null) ? findOrThrow(req.managerId()) : null;

        Set<SystemRole> roles = (req.systemRoles() != null && !req.systemRoles().isEmpty())
                ? req.systemRoles()
                : Set.of(SystemRole.ROLE_MEMBER);

        Member member = Member.builder()
                .fullName(req.fullName())
                .displayName(req.displayName())
                .email(req.email().toLowerCase())
                .region(req.region())
                .timezone(req.timezone())
                .status(MemberStatus.ACTIVE)
                .oncallEligible(req.oncallEligible())
                .joinDate(req.joinDate())
                .slackHandle(req.slackHandle())
                .phone(req.phone())
                .manager(manager)
                .systemRoles(roles)
                .build();

        member = memberRepository.save(member);

        // Store credentials in separate table
        credentialRepository.save(MemberCredential.builder()
                .memberId(member.getId())
                .passwordHash(passwordEncoder.encode(req.password()))
                .passwordChangedAt(Instant.now())
                .build());

        // Outbox event (same transaction)
        outboxPublisher.save("Member", member.getId().toString(), "MemberCreated", TOPIC,
                objectMapper.writeValueAsString(Map.of(
                        "memberId", member.getId(),
                        "email", member.getEmail(),
                        "roles", member.getSystemRoles()
                )));

        log.info("Member created: id={} email={}", member.getId(), member.getEmail());
        return mapper.toMemberResponse(member);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    @SneakyThrows
    public MemberResponse updateMember(UUID memberId, UpdateMemberRequest req, AuthenticatedMember actor) {
        Member member = findOrThrow(memberId);

        // Non-admins can only update themselves
        if (!actor.isAdmin() && !actor.memberId().equals(memberId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Only admins can change roles or status
        if (!actor.isAdmin()) {
            if (req.systemRoles() != null) throw new AccessDeniedException("Only admins may change system roles");
            if (req.status() != null) throw new AccessDeniedException("Only admins may change member status");
            if (req.managerId() != null) throw new AccessDeniedException("Only admins may change manager assignment");
        }

        if (req.fullName() != null)       member.setFullName(req.fullName());
        if (req.displayName() != null)    member.setDisplayName(req.displayName());
        if (req.region() != null)         member.setRegion(req.region());
        if (req.timezone() != null)       member.setTimezone(req.timezone());
        if (req.oncallEligible() != null) member.setOncallEligible(req.oncallEligible());
        if (req.joinDate() != null)       member.setJoinDate(req.joinDate());
        if (req.slackHandle() != null)    member.setSlackHandle(req.slackHandle());
        if (req.phone() != null)          member.setPhone(req.phone());
        if (req.managerId() != null)      member.setManager(findOrThrow(req.managerId()));
        if (req.systemRoles() != null)    member.setSystemRoles(req.systemRoles());
        if (req.status() != null)         member.setStatus(req.status());

        member = memberRepository.save(member);

        outboxPublisher.save("Member", member.getId().toString(), "MemberUpdated", TOPIC,
                objectMapper.writeValueAsString(Map.of(
                        "memberId", member.getId(),
                        "email", member.getEmail(),
                        "status", member.getStatus()
                )));

        return mapper.toMemberResponse(member);
    }

    // ── DEACTIVATE (soft-delete) ──────────────────────────────────────────────

    @Transactional
    @SneakyThrows
    public void deactivateMember(UUID memberId) {
        Member member = findOrThrow(memberId);
        member.setStatus(MemberStatus.DEACTIVATED);
        memberRepository.save(member);

        outboxPublisher.save("Member", memberId.toString(), "MemberDeactivated", TOPIC,
                objectMapper.writeValueAsString(Map.of("memberId", memberId)));

        log.info("Member deactivated: id={}", memberId);
    }

    // ── NOTIFICATION PREFERENCES ──────────────────────────────────────────────

    @Transactional
    @SneakyThrows
    public NotificationPreferenceResponse upsertNotificationPreference(
            UUID memberId, NotificationChannel channel,
            NotificationEventType eventType, boolean enabled) {

        // Mandatory notifications cannot be disabled
        if (!enabled && NON_DISABLEABLE.contains(eventType)) {
            throw new DomainException("Notification event " + eventType + " cannot be disabled");
        }

        NotificationPreference pref = notifPrefRepository
                .findByMemberIdAndChannelAndEventType(memberId, channel, eventType)
                .orElse(NotificationPreference.builder()
                        .member(findOrThrow(memberId))
                        .channel(channel)
                        .eventType(eventType)
                        .build());

        pref.setEnabled(enabled);
        pref = notifPrefRepository.save(pref);

        outboxPublisher.save("NotificationPreference", pref.getId().toString(),
                "NotificationPreferenceUpdated", TOPIC,
                objectMapper.writeValueAsString(Map.of(
                        "memberId", memberId,
                        "channel", channel,
                        "eventType", eventType,
                        "enabled", enabled
                )));

        return mapper.toNotificationPreferenceResponse(pref);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Member findOrThrow(UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }
}
