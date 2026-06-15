package com.oncall.teampolicy.controller;

import com.oncall.teampolicy.dto.request.ShiftDefinitionUpsertRequest;
import com.oncall.teampolicy.dto.response.ShiftDefinitionResponse;
import com.oncall.teampolicy.service.ShiftDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftDefinitionController {

    private final ShiftDefinitionService shiftDefinitionService;

    @GetMapping
    public ResponseEntity<List<ShiftDefinitionResponse>> list() {
        return ResponseEntity.ok(shiftDefinitionService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftDefinitionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftDefinitionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ShiftDefinitionResponse> create(@Valid @RequestBody ShiftDefinitionUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftDefinitionService.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShiftDefinitionResponse> update(@PathVariable UUID id, @Valid @RequestBody ShiftDefinitionUpsertRequest req) {
        return ResponseEntity.accepted().body(shiftDefinitionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        shiftDefinitionService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
