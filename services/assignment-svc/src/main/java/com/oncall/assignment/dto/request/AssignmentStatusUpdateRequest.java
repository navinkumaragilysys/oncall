package com.oncall.assignment.dto.request;

import com.oncall.domain.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record AssignmentStatusUpdateRequest(@NotNull AssignmentStatus status) {}
