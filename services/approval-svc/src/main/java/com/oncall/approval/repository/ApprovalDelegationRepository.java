package com.oncall.approval.repository;

import com.oncall.approval.entity.ApprovalDelegationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalDelegationRepository extends JpaRepository<ApprovalDelegationEntity, UUID> {

    List<ApprovalDelegationEntity> findByDelegatorId(UUID delegatorId);

    List<ApprovalDelegationEntity> findByDelegateeIdAndActiveTrue(UUID delegateeId);
}
