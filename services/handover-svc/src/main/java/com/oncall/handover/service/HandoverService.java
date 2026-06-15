package com.oncall.handover.service;

import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.handover.dto.request.HandoverRejectRequest;
import com.oncall.handover.dto.request.HandoverReportRequest;
import com.oncall.handover.dto.request.HandoverSubmitRequest;
import com.oncall.handover.dto.response.HandoverReportResponse;
import com.oncall.handover.entity.HandoverReportEntity;
import com.oncall.handover.entity.HandoverStatus;
import com.oncall.handover.repository.HandoverReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for handover lifecycle.
 *
 * <p>SOLID notes:</p>
 * <ul>
 *   <li>SRP — manages handover state machine only; HTTP concerns live in the controller.</li>
 *   <li>OCP — new status transitions are added as new methods; existing ones are unchanged.</li>
 *   <li>DIP — depends on {@link OutboxEventPublisher} abstraction, not the concrete publisher.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverReportRepository repo;
    private final OutboxEventPublisher outboxPublisher;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<HandoverReportResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<HandoverReportResponse> listByAssignment(UUID assignmentId) {
        return repo.findByAssignmentId(assignmentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HandoverReportResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Transactional
    public HandoverReportResponse create(HandoverReportRequest req) {
        HandoverReportEntity entity = new HandoverReportEntity();
        entity.setId(UUID.randomUUID());
        entity.setAssignmentId(req.assignmentId());
        entity.setFromMemberId(req.fromMemberId());
        entity.setToMemberId(req.toMemberId());
        entity.setScheduledAt(req.scheduledAt());
        entity.setSummary(req.summary());
        entity.setNotes(req.notes());
        entity.setStatus(HandoverStatus.PENDING);

        HandoverReportEntity saved = repo.save(entity);
        outboxPublisher.publish("handover_report", saved.getId(), "handover.created", toResponse(saved));
        return toResponse(saved);
    }

    /** Outgoing engineer submits the report (PENDING → SUBMITTED). */
    @Transactional
    public HandoverReportResponse submit(UUID id, HandoverSubmitRequest req) {
        HandoverReportEntity entity = fetch(id);
        requireStatus(entity, HandoverStatus.PENDING, "submit");

        entity.setSummary(req.summary());
        entity.setNotes(req.notes());
        entity.setStatus(HandoverStatus.SUBMITTED);
        entity.setSubmittedAt(Instant.now());

        HandoverReportEntity saved = repo.save(entity);
        outboxPublisher.publish("handover_report", saved.getId(), "handover.submitted", toResponse(saved));
        return toResponse(saved);
    }

    /** Incoming engineer acknowledges the handover (SUBMITTED → ACKNOWLEDGED). */
    @Transactional
    public HandoverReportResponse acknowledge(UUID id) {
        HandoverReportEntity entity = fetch(id);
        requireStatus(entity, HandoverStatus.SUBMITTED, "acknowledge");

        entity.setStatus(HandoverStatus.ACKNOWLEDGED);
        entity.setAcknowledgedAt(Instant.now());

        HandoverReportEntity saved = repo.save(entity);
        outboxPublisher.publish("handover_report", saved.getId(), "handover.acknowledged", toResponse(saved));
        return toResponse(saved);
    }

    /** Incoming engineer rejects the handover (SUBMITTED → REJECTED). */
    @Transactional
    public HandoverReportResponse reject(UUID id, HandoverRejectRequest req) {
        HandoverReportEntity entity = fetch(id);
        requireStatus(entity, HandoverStatus.SUBMITTED, "reject");

        entity.setStatus(HandoverStatus.REJECTED);
        entity.setRejectionReason(req.reason());

        HandoverReportEntity saved = repo.save(entity);
        outboxPublisher.publish("handover_report", saved.getId(), "handover.rejected", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        HandoverReportEntity entity = fetch(id);
        requireStatus(entity, HandoverStatus.PENDING, "delete");
        repo.delete(entity);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private HandoverReportEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handover report not found: " + id));
    }

    private void requireStatus(HandoverReportEntity entity, HandoverStatus required, String action) {
        if (entity.getStatus() != required) {
            throw new IllegalArgumentException(
                    "Cannot " + action + " a handover report in status " + entity.getStatus());
        }
    }

    private HandoverReportResponse toResponse(HandoverReportEntity e) {
        return new HandoverReportResponse(
                e.getId(), e.getAssignmentId(), e.getFromMemberId(), e.getToMemberId(),
                e.getStatus(), e.getSummary(), e.getNotes(), e.getRejectionReason(),
                e.getScheduledAt(), e.getSubmittedAt(), e.getAcknowledgedAt(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
