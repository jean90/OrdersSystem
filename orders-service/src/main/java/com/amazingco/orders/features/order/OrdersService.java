package com.amazingco.orders.features.order;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderNotFoundException;
import com.amazingco.core.valueobject.OrderId;

import java.util.Optional;

/**
 * Aggregate service port for {@link Order}. Takes/returns the aggregate itself rather than its
 * individual fields — building the aggregate from a command's values is the use case's job; this
 * port only deals in whole aggregates, mirroring {@code stocking-service}'s
 * {@code ProductsService}.
 */
public interface OrdersService {

    /**
     * Persists a new order. The caller (a use case) has already built it via {@link Order#create}
     * and added its lines.
     */
    Order create(Order order);

    /**
     * Throws {@link OrderNotFoundException} if no order exists for this id.
     */
    Order findById(OrderId orderId);

    /**
     * Persists changes to an order the caller has already loaded (via {@link #findById}) and
     * mutated — e.g. the order-transaction orchestrator cancelling it as compensation. Does not
     * re-check existence. Mirrors stocking-service's {@code ProductsService} create/update split.
     */
    Order update(Order order);

    /**
     * Backs the {@code Idempotency-Key} requirement on {@code POST /api/orders}: the order
     * already created for this key, if any.
     */
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    void recordIdempotencyKey(String idempotencyKey, OrderId orderId);
}
