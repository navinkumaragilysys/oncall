package com.oncall.availability.repository;

import com.oncall.availability.entity.SwapRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SwapRequestRepository extends JpaRepository<SwapRequestEntity, UUID> {
    List<SwapRequestEntity> findByRequestorId(UUID requestorId);
    List<SwapRequestEntity> findByAssignmentId(UUID assignmentId);
}
