package com.oncall.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.reporting.dto.request.ReportSnapshotRequest;
import com.oncall.reporting.dto.response.ReportSnapshotResponse;
import com.oncall.reporting.entity.ReportSnapshotEntity;
import com.oncall.reporting.entity.ReportType;
import com.oncall.reporting.repository.ReportSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Stores and queries materialised report snapshots.
 *
 * <p>SRP: manages report snapshots only; does not orchestrate data collection
 * from other services (that belongs to a separate aggregation pipeline).</p>
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportSnapshotRepository repo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ReportSnapshotResponse> list(ReportType reportType, UUID teamId) {
        if (reportType != null && teamId != null) {
            return repo.findByReportTypeAndTeamIdOrderByGeneratedAtDesc(reportType, teamId)
                    .stream().map(this::toResponse).toList();
        }
        if (reportType != null) {
            return repo.findByReportTypeOrderByGeneratedAtDesc(reportType)
                    .stream().map(this::toResponse).toList();
        }
        if (teamId != null) {
            return repo.findByTeamIdOrderByGeneratedAtDesc(teamId)
                    .stream().map(this::toResponse).toList();
        }
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReportSnapshotResponse getById(UUID id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report snapshot not found: " + id)));
    }

    @Transactional
    public ReportSnapshotResponse store(ReportSnapshotRequest req) {
        if (!req.periodEnd().isAfter(req.periodStart())) {
            throw new IllegalArgumentException("periodEnd must be after periodStart");
        }

        ReportSnapshotEntity entity = new ReportSnapshotEntity();
        entity.setId(UUID.randomUUID());
        entity.setReportType(req.reportType());
        entity.setTeamId(req.teamId());
        entity.setPeriodStart(req.periodStart());
        entity.setPeriodEnd(req.periodEnd());

        try {
            entity.setData(objectMapper.readTree(req.data()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in data field: " + e.getMessage());
        }

        return toResponse(repo.save(entity));
    }

    private ReportSnapshotResponse toResponse(ReportSnapshotEntity e) {
        return new ReportSnapshotResponse(
                e.getId(), e.getReportType(), e.getTeamId(),
                e.getPeriodStart(), e.getPeriodEnd(), e.getData(), e.getGeneratedAt()
        );
    }
}
