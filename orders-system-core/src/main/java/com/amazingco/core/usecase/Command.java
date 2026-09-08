package com.amazingco.core.usecase;

/**
 * Base type for every command accepted by a {@link UseCase}. Carries the {@code traceId}
 * generated at the API boundary and propagated through the saga (see the correlation-id
 * architecture note) so every use case can log it, regardless of aggregate.
 */
public abstract class Command {

    private final String traceId;

    protected Command(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
        this.traceId = traceId;
    }

    public String traceId() {
        return traceId;
    }
}
