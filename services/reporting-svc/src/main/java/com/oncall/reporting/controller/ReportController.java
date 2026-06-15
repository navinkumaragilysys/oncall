package com.oncall.reporting.controller;

import com.oncall.reporting.dto.request.ReportSnapshotRequest;
import com.oncall.reporting.dto.response.ReportSnapshotResponse;
import com.oncall.reporting.entity.ReportType;
import com.oncall.reporting.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Read-heavy reporting API.
 *
 * <p>SRP: HTTP mapping only; no business logic here.
 * OCP: new report types add enum values without changing this controller.</p>
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportSnapshotResponse>> list(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) UUID teamId) {
        return ResponseEntity.ok(reportService.list(reportType, teamId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportSnapshotResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    /** Store a pre-computed report snapshot (pushed by pipeline or on-demand generator). */
    @PostMapping
    public ResponseEntity<ReportSnapshotResponse> store(
            @Valid @RequestBody ReportSnapshotRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.store(req));
    }
}
