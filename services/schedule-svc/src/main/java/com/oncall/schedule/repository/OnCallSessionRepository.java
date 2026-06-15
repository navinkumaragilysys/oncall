package com.oncall.schedule.repository;

import com.oncall.domain.enums.SessionStatus;
import com.oncall.schedule.entity.OnCallSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OnCallSessionRepository extends JpaRepository<OnCallSessionEntity, UUID> {
    List<OnCallSessionEntity> findByTeamId(UUID teamId);

    List<OnCallSessionEntity> findByTeamIdAndRotationWeekStartGreaterThanEqualAndRotationWeekEndLessThanEqual(
            UUID teamId,
            Instant start,
            Instant end
    );

    List<OnCallSessionEntity> findByRotationWeekStartGreaterThanEqualAndRotationWeekEndLessThanEqual(
            Instant start,
            Instant end
    );

    List<OnCallSessionEntity> findByStatus(SessionStatus status);
}
