package com.oncall.approval.controller;

import com.oncall.approval.dto.request.ApprovalDecisionRequest;
import com.oncall.approval.dto.request.ApprovalRequestCreateRequest;
import com.oncall.approval.dto.response.ApprovalRequestResponse;
import com.oncall.approval.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * SRP: only maps HTTP ↔ service calls.
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalRequestService approvalService;

    @GetMapping
    public ResponseEntity<List<ApprovalRequestResponse>> list(
            @RequestParam(required = false) UUID approverId,
            @RequestParam(required = false) Boolean pending) {
        if (approverId != null) return ResponseEntity.ok(approvalService.listByApprover(approverId));
        if (Boolean.TRUE.equals(pending)) return ResponseEntity.ok(approvalService.listPending());
        return ResponseEntity.ok(approvalService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalRequestResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(approvalService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApprovalRequestResponse> create(
            @Valid @RequestBody ApprovalRequestCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(approvalService.create(req));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApprovalRequestResponse> approve(
            @PathVariable UUID id,
            @RequestBody ApprovalDecisionRequest req) {
        return ResponseEntity.ok(approvalService.approve(id, req));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApprovalRequestResponse> reject(
            @PathVariable UUID id,
            @RequestBody ApprovalDecisionRequest req) {
        return ResponseEntity.ok(approvalService.reject(id, req));
    }

    @PatchMapping("/{id}/delegate")
    public ResponseEntity<ApprovalRequestResponse> delegate(
            @PathVariable UUID id,
            @RequestParam UUID newApproverId) {
        return ResponseEntity.ok(approvalService.delegate(id, newApproverId));
    }
}
