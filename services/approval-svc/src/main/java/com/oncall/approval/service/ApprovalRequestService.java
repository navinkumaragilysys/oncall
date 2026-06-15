package com.oncall.approval.service;

import com.oncall.approval.dto.request.ApprovalDecisionRequest;
import com.oncall.approval.dto.request.ApprovalRequestCreateRequest;
import com.oncall.approval.dto.response.ApprovalRequestResponse;
import com.oncall.approval.entity.ApprovalRequestEntity;
import com.oncall.approval.entity.ApprovalStatus;
import com.oncall.approval.repository.ApprovalRequestRepository;
import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manages the lifecycle of approval requests.
 *
 * <p>SOLID notes:</p>
 * <ul>
 *   <li>SRP — handles approval state machine only.</li>
 *   <li>OCP — new decision types added as new methods, existing ones unchanged.</li>
 *   <li>DIP — depends on {@link OutboxEventPublisher} abstraction.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository repo;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> listByApprover(UUID approverId) {
        return repo.findByApproverId(approverId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> listPending() {
        return repo.findByStatus(ApprovalStatus.PENDING).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApprovalRequestResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    @Transactional
    public ApprovalRequestResponse create(ApprovalRequestCreateRequest req) {
        ApprovalRequestEntity entity = new ApprovalRequestEntity();
        entity.setId(UUID.randomUUID());
        entity.setReferenceType(req.referenceType());
        entity.setReferenceId(req.referenceId());
        entity.setRequestorId(req.requestorId());
        entity.setApproverId(req.approverId());
        entity.setStatus(ApprovalStatus.PENDING);

        ApprovalRequestEntity saved = repo.save(entity);
        outboxPublisher.publish("approval_request", saved.getId(), "approval.created", toResponse(saved));
        return toResponse(saved);
    }

    /** Manager approves the request (PENDING → APPROVED). */
    @Transactional
    public ApprovalRequestResponse approve(UUID id, ApprovalDecisionRequest req) {
        ApprovalRequestEntity entity = fetch(id);
        requireStatus(entity, ApprovalStatus.PENDING, "approve");

        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setDecisionReason(req.reason());
        entity.setDecidedAt(Instant.now());

        ApprovalRequestEntity saved = repo.save(entity);
        outboxPublisher.publish("approval_request", saved.getId(), "approval.approved", toResponse(saved));
        return toResponse(saved);
    }

    /** Manager rejects the request (PENDING → REJECTED). */
    @Transactional
    public ApprovalRequestResponse reject(UUID id, ApprovalDecisionRequest req) {
        ApprovalRequestEntity entity = fetch(id);
        requireStatus(entity, ApprovalStatus.PENDING, "reject");

        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setDecisionReason(req.reason());
        entity.setDecidedAt(Instant.now());

        ApprovalRequestEntity saved = repo.save(entity);
        outboxPublisher.publish("approval_request", saved.getId(), "approval.rejected", toResponse(saved));
        return toResponse(saved);
    }

    /** Delegate this approval to another manager (PENDING → DELEGATED). */
    @Transactional
    public ApprovalRequestResponse delegate(UUID id, UUID newApproverId) {
        ApprovalRequestEntity entity = fetch(id);
        requireStatus(entity, ApprovalStatus.PENDING, "delegate");

        entity.setStatus(ApprovalStatus.DELEGATED);
        entity.setApproverId(newApproverId);
        entity.setDecidedAt(Instant.now());

        ApprovalRequestEntity saved = repo.save(entity);
        outboxPublisher.publish("approval_request", saved.getId(), "approval.delegated", toResponse(saved));
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ApprovalRequestEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found: " + id));
    }

    private void requireStatus(ApprovalRequestEntity entity, ApprovalStatus required, String action) {
        if (entity.getStatus() != required) {
            throw new IllegalArgumentException(
                    "Cannot " + action + " an approval request in status " + entity.getStatus());
        }
    }

    private ApprovalRequestResponse toResponse(ApprovalRequestEntity e) {
        return new ApprovalRequestResponse(
                e.getId(), e.getReferenceType(), e.getReferenceId(),
                e.getRequestorId(), e.getApproverId(), e.getStatus(),
                e.getDecisionReason(), e.getDecidedAt(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
