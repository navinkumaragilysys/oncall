package com.oncall.identity.dto.response;

import com.oncall.domain.enums.NotificationChannel;
import com.oncall.domain.enums.NotificationEventType;

import java.util.UUID;

public record NotificationPreferenceResponse(
        UUID id,
        NotificationChannel channel,
        NotificationEventType eventType,
        boolean enabled
) {}
