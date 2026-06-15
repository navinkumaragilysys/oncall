package com.oncall.ticket.controller;

import com.oncall.ticket.dto.request.AdoOrgConfigRequest;
import com.oncall.ticket.dto.response.AdoOrgConfigResponse;
import com.oncall.ticket.service.AdoOrgConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ado-configs")
@RequiredArgsConstructor
public class AdoOrgConfigController {

    private final AdoOrgConfigService adoConfigService;

    @GetMapping
    public ResponseEntity<List<AdoOrgConfigResponse>> list() {
        return ResponseEntity.ok(adoConfigService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdoOrgConfigResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(adoConfigService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AdoOrgConfigResponse> create(@Valid @RequestBody AdoOrgConfigRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adoConfigService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AdoOrgConfigResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(adoConfigService.deactivate(id));
    }
}
