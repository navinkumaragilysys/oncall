package com.oncall.assignment.repository;

import com.oncall.assignment.entity.AssignmentEntity;
import com.oncall.domain.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
    List<AssignmentEntity> findBySessionId(UUID sessionId);
    List<AssignmentEntity> findByMemberId(UUID memberId);
    List<AssignmentEntity> findByTeamId(UUID teamId);
    List<AssignmentEntity> findByStatus(AssignmentStatus status);
}
