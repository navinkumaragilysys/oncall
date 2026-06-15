package com.oncall.assignment.repository;

import com.oncall.assignment.entity.OnCallHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OnCallHistoryRepository extends JpaRepository<OnCallHistoryEntity, UUID> {
    List<OnCallHistoryEntity> findByMemberId(UUID memberId);
    Optional<OnCallHistoryEntity> findByAssignmentId(UUID assignmentId);
}
