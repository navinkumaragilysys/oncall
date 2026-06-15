package com.oncall.availability.repository;

import com.oncall.availability.entity.MidweekReassignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MidweekReassignmentRepository extends JpaRepository<MidweekReassignmentEntity, UUID> {
    List<MidweekReassignmentEntity> findByAssignmentId(UUID assignmentId);
    List<MidweekReassignmentEntity> findByOriginalMemberId(UUID memberId);
}
