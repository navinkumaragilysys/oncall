package com.oncall.assignment.service;

import com.oncall.assignment.dto.request.HistoryCreateRequest;
import com.oncall.assignment.dto.response.HistoryResponse;
import com.oncall.assignment.entity.OnCallHistoryEntity;
import com.oncall.assignment.exception.ResourceNotFoundException;
import com.oncall.assignment.outbox.OutboxPublisher;
import com.oncall.assignment.repository.OnCallHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final OnCallHistoryRepository historyRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<HistoryResponse> list(UUID memberId) {
        return (memberId == null
                ? historyRepository.findAll()
                : historyRepository.findByMemberId(memberId))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HistoryResponse getById(UUID id) {
        return toResponse(historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found: " + id)));
    }

    @Transactional
    public HistoryResponse create(HistoryCreateRequest req) {
        OnCallHistoryEntity e = new OnCallHistoryEntity();
        apply(e, req);
        OnCallHistoryEntity saved = historyRepository.save(e);
        outboxPublisher.publish("oncall_history", saved.getId(), "oncall_history.created", toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        OnCallHistoryEntity e = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found: " + id));
        historyRepository.delete(e);
        outboxPublisher.publish("oncall_history", id, "oncall_history.deleted", "{}");
    }

    private void apply(OnCallHistoryEntity e, HistoryCreateRequest req) {
        e.setMemberId(req.memberId());
        e.setAssignmentId(req.assignmentId());
        e.setPeriodStart(req.periodStart());
        e.setPeriodEnd(req.periodEnd());
        e.setRole(req.role());
        e.setCompletionStatus(req.completionStatus());
        e.setHandoverStatus(req.handoverStatus());
        e.setFairnessCreditDays(req.fairnessCreditDays());
        e.setNotes(req.notes());
        e.setImported(req.imported());
    }

    private HistoryResponse toResponse(OnCallHistoryEntity e) {
        return new HistoryResponse(e.getId(), e.getMemberId(), e.getAssignmentId(),
                e.getPeriodStart(), e.getPeriodEnd(), e.getRole(), e.getCompletionStatus(),
                e.getHandoverStatus(), e.getFairnessCreditDays(), e.getNotes(),
                e.isImported(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
