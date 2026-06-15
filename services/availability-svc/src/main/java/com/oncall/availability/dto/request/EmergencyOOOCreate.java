package com.oncall.availability.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record EmergencyOOOCreate(
        @NotNull UUID memberId, @NotNull UUID reportedById,
        @NotNull UUID managerId, @NotNull Instant startTime,
        @NotNull Instant effectiveStartTime, String reason) {}
