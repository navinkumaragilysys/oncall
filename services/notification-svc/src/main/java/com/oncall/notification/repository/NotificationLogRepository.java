package com.oncall.notification.repository;

import com.oncall.notification.entity.NotificationLogEntity;
import com.oncall.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {
    List<NotificationLogEntity> findByRecipientId(UUID recipientId);
    List<NotificationLogEntity> findByStatus(NotificationStatus status);
    List<NotificationLogEntity> findByEventType(String eventType);
}
