package com.oncall.notification.dto.response;

import com.oncall.notification.entity.NotificationChannel;
import com.oncall.notification.entity.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        UUID recipientId,
        NotificationChannel channel,
        String eventType,
        String subject,
        NotificationStatus status,
        Instant sentAt,
        String failureReason,
        Instant createdAt
) {}
