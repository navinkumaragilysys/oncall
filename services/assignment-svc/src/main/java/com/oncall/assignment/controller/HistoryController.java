package com.oncall.assignment.controller;

import com.oncall.assignment.dto.request.HistoryCreateRequest;
import com.oncall.assignment.dto.response.HistoryResponse;
import com.oncall.assignment.service.HistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<HistoryResponse>> list(@RequestParam(required = false) UUID memberId) {
        return ResponseEntity.ok(historyService.list(memberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(historyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HistoryResponse> create(@Valid @RequestBody HistoryCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        historyService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
