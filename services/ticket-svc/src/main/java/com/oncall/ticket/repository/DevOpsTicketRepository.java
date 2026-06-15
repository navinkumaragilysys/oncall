package com.oncall.ticket.repository;

import com.oncall.ticket.entity.DevOpsTicketEntity;
import com.oncall.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DevOpsTicketRepository extends JpaRepository<DevOpsTicketEntity, UUID> {
    List<DevOpsTicketEntity> findByTeamId(UUID teamId);
    List<DevOpsTicketEntity> findByAssignmentId(UUID assignmentId);
    List<DevOpsTicketEntity> findByTicketStatus(TicketStatus status);
}
