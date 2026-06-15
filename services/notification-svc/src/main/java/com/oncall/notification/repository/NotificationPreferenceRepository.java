package com.oncall.notification.repository;

import com.oncall.notification.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, UUID> {
    List<NotificationPreferenceEntity> findByMemberId(UUID memberId);
}
