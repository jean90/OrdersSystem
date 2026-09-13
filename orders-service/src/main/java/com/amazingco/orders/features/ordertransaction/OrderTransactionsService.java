package com.amazingco.orders.features.ordertransaction;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionNotFoundException;
import com.amazingco.core.valueobject.OrderId;

/**
 * Aggregate service port for {@link OrderTransaction}, mirroring {@code OrdersService}'s shape.
 */
public interface OrderTransactionsService {

    /**
     * Starts a new order transaction (status {@code CREATED}) for this order and persists it.
     */
    OrderTransaction start(OrderId orderId);

    /**
     * Throws {@link OrderTransactionNotFoundException} if no transaction exists for this order —
     * a genuine error, since a transaction is always started before any result event can arrive.
     */
    OrderTransaction findByOrderId(OrderId orderId);

    OrderTransaction save(OrderTransaction orderTransaction);
}
