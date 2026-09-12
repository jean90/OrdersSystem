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
}
