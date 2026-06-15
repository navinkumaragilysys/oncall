package com.oncall.handover.dto.request;

/**
 * Payload for reject action — the incoming engineer provides a reason.
 */
public record HandoverRejectRequest(String reason) {}
