package com.oncall.handover.controller;

import com.oncall.handover.dto.request.HandoverRejectRequest;
import com.oncall.handover.dto.request.HandoverReportRequest;
import com.oncall.handover.dto.request.HandoverSubmitRequest;
import com.oncall.handover.dto.response.HandoverReportResponse;
import com.oncall.handover.service.HandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the handover lifecycle.
 *
 * <p>SRP: only maps HTTP requests to service calls and HTTP responses.
 * No business logic here.</p>
 */
@RestController
@RequestMapping("/api/v1/handovers")
@RequiredArgsConstructor
public class HandoverController {

    private final HandoverService handoverService;

    @GetMapping
    public ResponseEntity<List<HandoverReportResponse>> list(
            @RequestParam(required = false) UUID assignmentId) {
        List<HandoverReportResponse> result = assignmentId != null
                ? handoverService.listByAssignment(assignmentId)
                : handoverService.list();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HandoverReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(handoverService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HandoverReportResponse> create(@Valid @RequestBody HandoverReportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handoverService.create(req));
    }

    /** Outgoing engineer submits the handover report (PENDING → SUBMITTED). */
    @PatchMapping("/{id}/submit")
    public ResponseEntity<HandoverReportResponse> submit(
            @PathVariable UUID id,
            @RequestBody HandoverSubmitRequest req) {
        return ResponseEntity.ok(handoverService.submit(id, req));
    }

    /** Incoming engineer acknowledges receipt (SUBMITTED → ACKNOWLEDGED). */
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<HandoverReportResponse> acknowledge(@PathVariable UUID id) {
        return ResponseEntity.ok(handoverService.acknowledge(id));
    }

    /** Incoming engineer rejects the handover (SUBMITTED → REJECTED). */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<HandoverReportResponse> reject(
            @PathVariable UUID id,
            @RequestBody HandoverRejectRequest req) {
        return ResponseEntity.ok(handoverService.reject(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        handoverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
