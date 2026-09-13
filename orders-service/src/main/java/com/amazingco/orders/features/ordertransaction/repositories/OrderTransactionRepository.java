package com.amazingco.orders.features.ordertransaction.repositories;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.valueobject.OrderId;

import java.util.Optional;

public interface OrderTransactionRepository {

    OrderTransaction save(OrderTransaction orderTransaction);

    Optional<OrderTransaction> findByOrderId(OrderId orderId);
}
