package com.amazingco.core.correlation;

import java.util.UUID;

/**
 * Resolves the {@code X-Trace-Id} request header into a
 * {@link com.amazingco.core.usecase.Command}'s required traceId, generating a new one if
 * absent/blank. Shared across every service's API boundary — see the correlation-id
 * architecture note: a traceId is generated where a request enters the system and
 * propagated from there through the saga.
 */
public final class TraceIds {

    public static final String HEADER = "X-Trace-Id";

    private TraceIds() {
    }

    public static String resolve(String headerValue) {
        return (headerValue == null || headerValue.isBlank()) ? UUID.randomUUID().toString() : headerValue;
    }
}
