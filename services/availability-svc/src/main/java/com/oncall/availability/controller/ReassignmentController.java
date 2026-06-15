package com.oncall.availability.controller;

import com.oncall.availability.dto.request.ReassignmentCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.ReassignmentResponse;
import com.oncall.availability.service.ReassignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/reassignments") @RequiredArgsConstructor
public class ReassignmentController {
    private final ReassignmentService svc;

    @GetMapping public ResponseEntity<List<ReassignmentResponse>> list(@RequestParam(required = false) UUID assignmentId) { return ResponseEntity.ok(svc.list(assignmentId)); }
    @GetMapping("/{id}") public ResponseEntity<ReassignmentResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<ReassignmentResponse> create(@Valid @RequestBody ReassignmentCreate req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PatchMapping("/{id}/status") public ResponseEntity<ReassignmentResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest req) { return ResponseEntity.accepted().body(svc.updateStatus(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { svc.delete(id); return ResponseEntity.accepted().build(); }
}
