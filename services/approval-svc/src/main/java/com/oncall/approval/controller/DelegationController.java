package com.oncall.approval.controller;

import com.oncall.approval.dto.request.DelegationCreateRequest;
import com.oncall.approval.dto.response.DelegationResponse;
import com.oncall.approval.service.DelegationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delegations")
@RequiredArgsConstructor
public class DelegationController {

    private final DelegationService delegationService;

    @GetMapping
    public ResponseEntity<List<DelegationResponse>> list(
            @RequestParam(required = false) UUID delegatorId) {
        List<DelegationResponse> result = delegatorId != null
                ? delegationService.listByDelegator(delegatorId)
                : delegationService.list();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DelegationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(delegationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DelegationResponse> create(@Valid @RequestBody DelegationCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(delegationService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DelegationResponse> revoke(@PathVariable UUID id) {
        return ResponseEntity.ok(delegationService.revoke(id));
    }
}
