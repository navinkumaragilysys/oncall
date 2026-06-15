package com.oncall.worklog.controller;

import com.oncall.worklog.dto.request.WorkLogCreateRequest;
import com.oncall.worklog.dto.request.WorkLogEventRequest;
import com.oncall.worklog.dto.response.WorkLogResponse;
import com.oncall.worklog.service.WorkLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/worklogs")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    @GetMapping
    public ResponseEntity<List<WorkLogResponse>> list(
            @RequestParam(required = false) UUID assignmentId,
            @RequestParam(required = false) UUID memberId) {
        return ResponseEntity.ok(workLogService.list(assignmentId, memberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkLogResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(workLogService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkLogResponse> create(@Valid @RequestBody WorkLogCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workLogService.create(req));
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<WorkLogResponse> addEvent(
            @PathVariable UUID id,
            @Valid @RequestBody WorkLogEventRequest req) {
        return ResponseEntity.accepted().body(workLogService.addEvent(id, req));
    }
}
