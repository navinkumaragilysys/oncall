package com.oncall.schedule.controller;

import com.oncall.domain.enums.SessionStatus;
import com.oncall.schedule.dto.request.SessionStatusUpdateRequest;
import com.oncall.schedule.dto.request.SessionUpsertRequest;
import com.oncall.schedule.dto.response.SessionResponse;
import com.oncall.schedule.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionResponse>> list(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) SessionStatus status) {
        return ResponseEntity.ok(sessionService.list(teamId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SessionUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SessionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SessionUpsertRequest req) {
        return ResponseEntity.accepted().body(sessionService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SessionResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody SessionStatusUpdateRequest req) {
        return ResponseEntity.accepted().body(sessionService.updateStatus(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sessionService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
