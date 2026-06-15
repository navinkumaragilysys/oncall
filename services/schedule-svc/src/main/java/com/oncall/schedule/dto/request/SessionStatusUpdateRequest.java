package com.oncall.schedule.dto.request;

import com.oncall.domain.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;

public record SessionStatusUpdateRequest(@NotNull SessionStatus status) {
}
