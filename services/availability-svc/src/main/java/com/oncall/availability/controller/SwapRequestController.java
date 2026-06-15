package com.oncall.availability.controller;

import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.request.SwapRequestCreate;
import com.oncall.availability.dto.response.SwapRequestResponse;
import com.oncall.availability.service.SwapRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/swaps") @RequiredArgsConstructor
public class SwapRequestController {
    private final SwapRequestService svc;

    @GetMapping public ResponseEntity<List<SwapRequestResponse>> list(@RequestParam(required = false) UUID requestorId) { return ResponseEntity.ok(svc.list(requestorId)); }
    @GetMapping("/{id}") public ResponseEntity<SwapRequestResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<SwapRequestResponse> create(@Valid @RequestBody SwapRequestCreate req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PatchMapping("/{id}/status") public ResponseEntity<SwapRequestResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest req) { return ResponseEntity.accepted().body(svc.updateStatus(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { svc.delete(id); return ResponseEntity.accepted().build(); }
}
