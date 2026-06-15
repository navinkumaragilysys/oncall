package com.oncall.notification.dto.request;

import com.oncall.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationPreferenceRequest(
        @NotNull UUID memberId,
        @NotNull NotificationChannel channel,
        @NotBlank String eventType,
        boolean enabled
) {}
