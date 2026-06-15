package com.oncall.availability.dto.request;

import com.oncall.domain.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull RequestStatus status, String rejectionReason) {}
