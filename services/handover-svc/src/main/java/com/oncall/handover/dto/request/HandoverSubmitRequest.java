package com.oncall.handover.dto.request;

/**
 * Payload for submit action — the outgoing engineer fills in the summary before
 * handing over.
 */
public record HandoverSubmitRequest(String summary, String notes) {}
