package com.oncall.assignment.controller;

import com.oncall.assignment.dto.request.AssignmentCreateRequest;
import com.oncall.assignment.dto.request.AssignmentStatusUpdateRequest;
import com.oncall.assignment.dto.response.AssignmentResponse;
import com.oncall.assignment.service.AssignmentService;
import com.oncall.domain.enums.AssignmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> list(
            @RequestParam(required = false) UUID sessionId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) AssignmentStatus status) {
        return ResponseEntity.ok(assignmentService.list(sessionId, memberId, teamId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssignmentResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody AssignmentStatusUpdateRequest req) {
        return ResponseEntity.accepted().body(assignmentService.updateStatus(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assignmentService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
