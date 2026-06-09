package com.oncall.identity.dto.request;

import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;
import jakarta.validation.constraints.NotNull;

public record UpsertNotificationPreferenceRequest(
        @NotNull NotificationChannel channel,
        @NotNull NotificationEventType eventType,
        @NotNull Boolean enabled
) {}
