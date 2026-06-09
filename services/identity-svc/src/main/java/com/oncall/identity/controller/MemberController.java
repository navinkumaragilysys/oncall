package com.oncall.identity.controller;

import com.oncall.domain.enums.MemberStatus;
import com.oncall.identity.dto.request.CreateMemberRequest;
import com.oncall.identity.dto.request.UpsertNotificationPreferenceRequest;
import com.oncall.identity.dto.request.UpdateMemberRequest;
import com.oncall.identity.dto.response.MemberResponse;
import com.oncall.identity.dto.response.NotificationPreferenceResponse;
import com.oncall.identity.security.AuthenticatedMember;
import com.oncall.identity.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ── READ ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedMember actor) {
        // Members can read themselves; admins/managers can read anyone
        if (!actor.isAdmin() && !actor.isManager() && !actor.memberId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(memberService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<MemberResponse>> listMembers(
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 25) Pageable pageable) {
        return ResponseEntity.ok(memberService.listMembers(status, pageable));
    }

    @GetMapping("/{id}/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceResponse>> getNotificationPreferences(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedMember actor) {
        if (!actor.isAdmin() && !actor.memberId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(memberService.getNotificationPreferences(id));
    }

    // ── CREATE — returns 202 (async via outbox → Kafka) ──────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest req) {
        MemberResponse created = memberService.createMember(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── UPDATE — returns 202 ──────────────────────────────────────────────────

    @PatchMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMemberRequest req,
            @AuthenticationPrincipal AuthenticatedMember actor) {
        return ResponseEntity.accepted().body(memberService.updateMember(id, req, actor));
    }

    // ── DEACTIVATE — soft-delete, returns 202 ────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateMember(@PathVariable UUID id) {
        memberService.deactivateMember(id);
        return ResponseEntity.accepted().build();
    }

    // ── NOTIFICATION PREFERENCES ──────────────────────────────────────────────

    @PutMapping("/{id}/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponse> upsertNotificationPreference(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertNotificationPreferenceRequest req,
            @AuthenticationPrincipal AuthenticatedMember actor) {
        if (!actor.isAdmin() && !actor.memberId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.accepted()
                .body(memberService.upsertNotificationPreference(
                        id, req.channel(), req.eventType(), req.enabled()));
    }
}
