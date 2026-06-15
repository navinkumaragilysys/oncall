package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.SubSessionUpsertRequest;
import com.oncall.teampolicy.dto.response.SubSessionResponse;
import com.oncall.teampolicy.service.SubSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sub-sessions")
@RequiredArgsConstructor
public class SubSessionController {

    private final SubSessionService subSessionService;

    @GetMapping
    public ResponseEntity<List<SubSessionResponse>> list(@RequestParam(required = false) UUID policyId) {
        return ResponseEntity.ok(subSessionService.list(policyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubSessionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(subSessionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SubSessionResponse> create(@Valid @RequestBody SubSessionUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subSessionService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubSessionResponse> update(@PathVariable UUID id, @Valid @RequestBody SubSessionUpsertRequest req) {
        return ResponseEntity.accepted().body(subSessionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        subSessionService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
