package com.oncall.audit.controller;

import com.oncall.audit.dto.request.AuditEntryRequest;
import com.oncall.audit.dto.response.AuditTrailResponse;
import com.oncall.audit.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Audit trail REST API.
 *
 * <p>SRP: only maps HTTP ↔ service calls. No business logic here.
 * OCP: read endpoints are separate from the write (append) endpoint — clients
 * query without affecting or depending on the write path.</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditTrailResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditService.listPaged(page, size));
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<List<AuditTrailResponse>> listByActor(@PathVariable UUID actorId) {
        return ResponseEntity.ok(auditService.listByActor(actorId));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<Page<AuditTrailResponse>> listByResource(
            @PathVariable String resourceType,
            @PathVariable UUID resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditService.listByResource(resourceType, resourceId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditTrailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(auditService.getById(id));
    }

    /** Append-only — no PUT/PATCH/DELETE endpoints. */
    @PostMapping
    public ResponseEntity<AuditTrailResponse> record(@Valid @RequestBody AuditEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditService.record(req));
    }
}
