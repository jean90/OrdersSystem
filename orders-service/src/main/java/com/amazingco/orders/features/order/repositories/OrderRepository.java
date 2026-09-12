package com.amazingco.orders.features.order.repositories;

import com.amazingco.core.order.Order;
import com.amazingco.core.valueobject.OrderId;

import java.util.Optional;

/**
 * Persistence port for {@link Order}. Unlike {@code stocking-service}'s {@code StockRepository}
 * (deliberately atomic-conditional-update only, no load/mutate/save), a full aggregate load/
 * mutate/save is the right fit here — {@code Order} is a genuine multi-field aggregate with
 * business logic in between reads and writes, not a single racy counter (see the concurrency
 * architecture note's distinction between the two cases).
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    /**
     * Backs the {@code Idempotency-Key} requirement on {@code POST /api/orders}. Returns the id
     * of the order already created for this key, if any.
     */
    Optional<OrderId> findOrderIdByIdempotencyKey(String idempotencyKey);

    /**
     * Records that {@code idempotencyKey} produced {@code orderId}. Relies on the
     * {@code order_idempotency_key} table's primary key to reject a duplicate key outright
     * (see {@code V2__create_order_idempotency_key_table.sql}) — this doesn't itself guard
     * against two truly concurrent requests both passing the "not found yet" check before
     * either calls this; the second one's insert would fail its unique-key constraint. Same
     * class of accepted simplification as stocking-service's ReserveStockUseCase.
     */
    void recordIdempotencyKey(String idempotencyKey, OrderId orderId);
}
