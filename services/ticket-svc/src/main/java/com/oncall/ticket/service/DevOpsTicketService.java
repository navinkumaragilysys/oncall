package com.oncall.ticket.service;

import com.oncall.common.exception.ResourceNotFoundException;
import com.oncall.common.outbox.OutboxEventPublisher;
import com.oncall.ticket.dto.request.DevOpsTicketRequest;
import com.oncall.ticket.dto.request.TicketSyncRequest;
import com.oncall.ticket.dto.response.DevOpsTicketResponse;
import com.oncall.ticket.entity.DevOpsTicketEntity;
import com.oncall.ticket.entity.TicketStatus;
import com.oncall.ticket.repository.DevOpsTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SRP: manages DevOps ticket records only.
 * DIP: depends on OutboxEventPublisher abstraction.
 */
@Service
@RequiredArgsConstructor
public class DevOpsTicketService {

    private final DevOpsTicketRepository repo;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional(readOnly = true)
    public List<DevOpsTicketResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DevOpsTicketResponse> listByTeam(UUID teamId) {
        return repo.findByTeamId(teamId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DevOpsTicketResponse getById(UUID id) {
        return toResponse(fetch(id));
    }

    @Transactional
    public DevOpsTicketResponse create(DevOpsTicketRequest req) {
        DevOpsTicketEntity entity = new DevOpsTicketEntity();
        entity.setId(UUID.randomUUID());
        entity.setTeamId(req.teamId());
        entity.setAssignmentId(req.assignmentId());
        entity.setWorklogId(req.worklogId());
        entity.setAdoTicketId(req.adoTicketId());
        entity.setAdoUrl(req.adoUrl());
        entity.setTicketType(req.ticketType());
        entity.setSummary(req.summary());
        entity.setTicketStatus(TicketStatus.CREATED);

        DevOpsTicketEntity saved = repo.save(entity);
        outboxPublisher.publish("devops_ticket", saved.getId(), "ticket.created", toResponse(saved));
        return toResponse(saved);
    }

    /** Sync the latest status from ADO. */
    @Transactional
    public DevOpsTicketResponse sync(UUID id, TicketSyncRequest req) {
        DevOpsTicketEntity entity = fetch(id);
        entity.setTicketStatus(req.ticketStatus());
        entity.setSyncedAt(Instant.now());

        DevOpsTicketEntity saved = repo.save(entity);
        outboxPublisher.publish("devops_ticket", saved.getId(), "ticket.synced", toResponse(saved));
        return toResponse(saved);
    }

    private DevOpsTicketEntity fetch(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DevOps ticket not found: " + id));
    }

    private DevOpsTicketResponse toResponse(DevOpsTicketEntity e) {
        return new DevOpsTicketResponse(
                e.getId(), e.getTeamId(), e.getAssignmentId(), e.getWorklogId(),
                e.getAdoTicketId(), e.getAdoUrl(), e.getTicketType(), e.getSummary(),
                e.getTicketStatus(), e.getSyncedAt(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
