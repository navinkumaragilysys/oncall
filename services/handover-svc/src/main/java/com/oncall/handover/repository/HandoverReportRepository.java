package com.oncall.handover.repository;

import com.oncall.handover.entity.HandoverReportEntity;
import com.oncall.handover.entity.HandoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * ISP: exposes only the query methods that consumers actually need.
 */
public interface HandoverReportRepository extends JpaRepository<HandoverReportEntity, UUID> {

    List<HandoverReportEntity> findByAssignmentId(UUID assignmentId);

    List<HandoverReportEntity> findByFromMemberId(UUID fromMemberId);

    List<HandoverReportEntity> findByToMemberId(UUID toMemberId);

    List<HandoverReportEntity> findByStatus(HandoverStatus status);
}
