package com.oncall.availability.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SwapRequestCreate(
        @NotNull UUID requestorId, @NotNull UUID targetMemberId,
        @NotNull UUID assignmentId, @NotNull UUID managerId,
        @NotBlank String reason) {}
