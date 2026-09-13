package com.amazingco.orders.features.ordertransaction.repositories;

import com.amazingco.core.order.Order;
import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionStatus;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.AbstractPostgresIntegrationTest;
import com.amazingco.orders.features.order.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTransactionRepositoryImplIT extends AbstractPostgresIntegrationTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private OrderTransactionRepository orderTransactionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM order_transaction");
        jdbcTemplate.update("DELETE FROM order_idempotency_key");
        jdbcTemplate.update("DELETE FROM order_line");
        jdbcTemplate.update("DELETE FROM orders");
    }

    private OrderId seedOrder() {
        Order order = Order.create(CustomerId.newId());
        order.addLine(SKU, Quantity.of(1), Money.of("9.99", "USD"));
        return orderRepository.save(order).orderId();
    }

    @Test
    void savePersistsANewTransactionInCreatedStatus() {
        OrderId orderId = seedOrder();

        orderTransactionRepository.save(OrderTransaction.start(orderId));

        OrderTransaction found = orderTransactionRepository.findByOrderId(orderId).orElseThrow();
        assertEquals(orderId, found.orderId());
        assertEquals(OrderTransactionStatus.CREATED, found.status());
    }

    @Test
    void findByOrderIdReturnsEmptyWhenUnknown() {
        assertTrue(orderTransactionRepository.findByOrderId(OrderId.newId()).isEmpty());
    }

    @Test
    void savingAnUpdatedTransactionPersistsTheStatusChange() {
        OrderId orderId = seedOrder();
        orderTransactionRepository.save(OrderTransaction.start(orderId));

        OrderTransaction loaded = orderTransactionRepository.findByOrderId(orderId).orElseThrow();
        loaded.markStockReserved();
        orderTransactionRepository.save(loaded);

        OrderTransaction reloaded = orderTransactionRepository.findByOrderId(orderId).orElseThrow();
        assertEquals(OrderTransactionStatus.STOCK_RESERVED, reloaded.status());
    }
}
