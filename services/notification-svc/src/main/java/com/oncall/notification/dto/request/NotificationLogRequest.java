package com.oncall.notification.dto.request;

import com.oncall.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationLogRequest(
        @NotNull UUID recipientId,
        @NotNull NotificationChannel channel,
        @NotBlank String eventType,
        String subject,
        String body
) {}
