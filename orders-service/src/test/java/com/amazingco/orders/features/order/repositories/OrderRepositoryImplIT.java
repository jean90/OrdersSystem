package com.amazingco.orders.features.order.repositories;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderStatus;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRepositoryImplIT extends AbstractPostgresIntegrationTest {

    private static final Sku SKU_A = new Sku("ABC-123");
    private static final Sku SKU_B = new Sku("XYZ-789");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM order_line");
        jdbcTemplate.update("DELETE FROM orders");
    }

    @Test
    void savePersistsANewOrderWithAllLinesAndTotal() {
        Order order = Order.create(CustomerId.newId());
        order.addLine(SKU_A, Quantity.of(2), Money.of("9.99", "USD"));
        order.addLine(SKU_B, Quantity.of(1), Money.of("5.00", "USD"));

        orderRepository.save(order);

        Order found = orderRepository.findById(order.orderId()).orElseThrow();
        assertEquals(order.orderId(), found.orderId());
        assertEquals(order.customerId(), found.customerId());
        assertEquals(OrderStatus.PENDING, found.status());
        assertEquals(2, found.lines().size());
        assertEquals(Money.of("24.98", "USD"), found.totalAmount());
    }

    @Test
    void findByIdReturnsEmptyWhenUnknown() {
        assertTrue(orderRepository.findById(OrderId.newId()).isEmpty());
    }

    @Test
    void savingAnUpdatedOrderReplacesItsLines() {
        Order order = Order.create(CustomerId.newId());
        order.addLine(SKU_A, Quantity.of(2), Money.of("9.99", "USD"));
        orderRepository.save(order);

        Order loaded = orderRepository.findById(order.orderId()).orElseThrow();
        loaded.addLine(SKU_B, Quantity.of(1), Money.of("5.00", "USD"));
        orderRepository.save(loaded);

        Order reloaded = orderRepository.findById(order.orderId()).orElseThrow();
        assertEquals(2, reloaded.lines().size());
        assertEquals(Money.of("24.98", "USD"), reloaded.totalAmount());
    }

    @Test
    void savingAConfirmedOrderPersistsTheStatusChange() {
        Order order = Order.create(CustomerId.newId());
        order.addLine(SKU_A, Quantity.of(1), Money.of("9.99", "USD"));
        orderRepository.save(order);

        Order loaded = orderRepository.findById(order.orderId()).orElseThrow();
        loaded.confirm();
        orderRepository.save(loaded);

        Order reloaded = orderRepository.findById(order.orderId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, reloaded.status());
    }
}
