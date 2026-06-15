package com.oncall.availability.repository;

import com.oncall.availability.entity.LeaveRequestEntity;
import com.oncall.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, UUID> {
    List<LeaveRequestEntity> findByMemberId(UUID memberId);
    List<LeaveRequestEntity> findByStatus(RequestStatus status);
}
