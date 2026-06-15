package com.oncall.notification.dto.response;

import com.oncall.notification.entity.NotificationChannel;

import java.time.Instant;
import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        UUID memberId,
        NotificationChannel channel,
        String eventType,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
