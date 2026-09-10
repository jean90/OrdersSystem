package com.amazingco.stocking.web;

import java.util.UUID;

/**
 * Resolves the {@code X-Trace-Id} request header into a {@link com.amazingco.core.usecase.Command}'s
 * required traceId, generating a new one if absent/blank. Stand-in for the API-boundary
 * traceId generation CLAUDE.md assigns to {@code orders-service} (which doesn't exist yet) —
 * revisit once that service's saga-boundary generation lands.
 */
public final class TraceIds {

    public static final String HEADER = "X-Trace-Id";

    private TraceIds() {
    }

    public static String resolve(String headerValue) {
        return (headerValue == null || headerValue.isBlank()) ? UUID.randomUUID().toString() : headerValue;
    }
}
