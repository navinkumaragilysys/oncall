package com.oncall.availability.controller;

import com.oncall.availability.dto.request.LeaveRequestCreate;
import com.oncall.availability.dto.request.StatusUpdateRequest;
import com.oncall.availability.dto.response.LeaveRequestResponse;
import com.oncall.availability.service.LeaveRequestService;
import com.oncall.domain.enums.RequestStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/leave") @RequiredArgsConstructor
public class LeaveRequestController {
    private final LeaveRequestService svc;

    @GetMapping public ResponseEntity<List<LeaveRequestResponse>> list(
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(svc.list(memberId, status));
    }
    @GetMapping("/{id}") public ResponseEntity<LeaveRequestResponse> getById(@PathVariable UUID id) { return ResponseEntity.ok(svc.getById(id)); }
    @PostMapping public ResponseEntity<LeaveRequestResponse> create(@Valid @RequestBody LeaveRequestCreate req) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req)); }
    @PatchMapping("/{id}/status") public ResponseEntity<LeaveRequestResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest req) { return ResponseEntity.accepted().body(svc.updateStatus(id, req)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { svc.delete(id); return ResponseEntity.accepted().build(); }
}
