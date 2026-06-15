package com.oncall.approval.repository;

import com.oncall.approval.entity.ApprovalReferenceType;
import com.oncall.approval.entity.ApprovalRequestEntity;
import com.oncall.approval.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequestEntity, UUID> {

    List<ApprovalRequestEntity> findByApproverId(UUID approverId);

    List<ApprovalRequestEntity> findByRequestorId(UUID requestorId);

    List<ApprovalRequestEntity> findByStatus(ApprovalStatus status);

    List<ApprovalRequestEntity> findByReferenceTypeAndReferenceId(
            ApprovalReferenceType referenceType, UUID referenceId);
}
