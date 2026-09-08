package com.amazingco.core.usecase;

/**
 * Uniform shape for the use-case (application/input-port) layer: one implementation per
 * business operation, taking a dedicated command object and returning its result.
 * Implementations are the transactional boundary in each service and are called by
 * adapters (REST controllers, Kafka listeners) — they should stay thin, delegating the
 * actual CRUD/validation work to the relevant aggregate service.
 * <p>
 * Lives in core (rather than in a single service) since both {@code orders-service}
 * and {@code stocking-service} structure their application layer around it.
 *
 * @param <C> the command type carrying this use case's input
 * @param <R> the result type returned on success
 */
public interface UseCase<C, R> {

    R execute(C command);
}
