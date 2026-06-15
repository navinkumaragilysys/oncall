package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.TeamUpsertRequest;
import com.oncall.teampolicy.dto.response.TeamResponse;
import com.oncall.teampolicy.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamResponse>> list() {
        return ResponseEntity.ok(teamService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TeamResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TeamUpsertRequest req) {
        return ResponseEntity.accepted().body(teamService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        teamService.deactivate(id);
        return ResponseEntity.accepted().build();
    }
}
