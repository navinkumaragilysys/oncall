package com.oncall.worklog.repository;

import com.oncall.worklog.entity.WorkLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkLogRepository extends JpaRepository<WorkLogEntity, UUID> {
    List<WorkLogEntity> findByAssignmentId(UUID assignmentId);
    List<WorkLogEntity> findByMemberId(UUID memberId);
    Optional<WorkLogEntity> findByAssignmentIdAndMemberIdAndLogDate(UUID assignmentId, UUID memberId, LocalDate logDate);
}
