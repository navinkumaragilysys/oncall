package com.oncall.availability.controller;

import com.oncall.availability.dto.request.EmergencyOOOCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.EmergencyOOOResponse;
import com.oncall.availability.service.EmergencyOOOService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/emergency-ooo") @RequiredArgsConstructor
public class EmergencyOOOController {
    private final EmergencyOOOService svc;

    @GetMapping public ResponseEntity<List<EmergencyOOOResponse>> list(@RequestParam(required = false) UUID memberId) { return ResponseEntity.ok(svc.list(memberId)); }
    @GetMapping("/{id}") public ResponseEntity<EmergencyOOOResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<EmergencyOOOResponse> create(@Valid @RequestBody EmergencyOOOCreate req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PatchMapping("/{id}/status") public ResponseEntity<EmergencyOOOResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest req) { return ResponseEntity.accepted().body(svc.updateStatus(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { svc.delete(id); return ResponseEntity.accepted().build(); }
}
