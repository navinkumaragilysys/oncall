package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.RotationPolicyUpsertRequest;
import com.oncall.teampolicy.dto.response.RotationPolicyResponse;
import com.oncall.teampolicy.service.RotationPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class RotationPolicyController {

    private final RotationPolicyService rotationPolicyService;

    @GetMapping
    public ResponseEntity<List<RotationPolicyResponse>> list() {
        return ResponseEntity.ok(rotationPolicyService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RotationPolicyResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(rotationPolicyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RotationPolicyResponse> create(@Valid @RequestBody RotationPolicyUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rotationPolicyService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RotationPolicyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RotationPolicyUpsertRequest req) {
        return ResponseEntity.accepted().body(rotationPolicyService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        rotationPolicyService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
