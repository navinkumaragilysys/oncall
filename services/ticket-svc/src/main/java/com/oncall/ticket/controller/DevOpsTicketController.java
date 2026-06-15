package com.oncall.ticket.controller;

import com.oncall.ticket.dto.request.DevOpsTicketRequest;
import com.oncall.ticket.dto.request.TicketSyncRequest;
import com.oncall.ticket.dto.response.DevOpsTicketResponse;
import com.oncall.ticket.service.DevOpsTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class DevOpsTicketController {

    private final DevOpsTicketService ticketService;

    @GetMapping
    public ResponseEntity<List<DevOpsTicketResponse>> list(
            @RequestParam(required = false) UUID teamId) {
        List<DevOpsTicketResponse> result = teamId != null
                ? ticketService.listByTeam(teamId)
                : ticketService.list();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevOpsTicketResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DevOpsTicketResponse> create(@Valid @RequestBody DevOpsTicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(req));
    }

    @PatchMapping("/{id}/sync")
    public ResponseEntity<DevOpsTicketResponse> sync(
            @PathVariable UUID id,
            @Valid @RequestBody TicketSyncRequest req) {
        return ResponseEntity.ok(ticketService.sync(id, req));
    }
}
