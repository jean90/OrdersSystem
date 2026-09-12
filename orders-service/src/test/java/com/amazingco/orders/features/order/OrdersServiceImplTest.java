package com.amazingco.orders.features.order;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderNotFoundException;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.order.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    private OrdersService ordersService;

    @BeforeEach
    void setUp() {
        ordersService = new OrdersServiceImpl(orderRepository);
    }

    @Test
    void createDelegatesToRepository() {
        Order order = Order.create(CustomerId.newId());
        when(orderRepository.save(order)).thenReturn(order);

        Order result = ordersService.create(order);

        assertSame(order, result);
    }

    @Test
    void findByIdReturnsOrderWhenPresent() {
        Order order = Order.create(CustomerId.newId());
        when(orderRepository.findById(order.orderId())).thenReturn(Optional.of(order));

        assertSame(order, ordersService.findById(order.orderId()));
    }

    @Test
    void findByIdThrowsWhenAbsent() {
        OrderId orderId = OrderId.newId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> ordersService.findById(orderId));
    }

    @Test
    void findByIdempotencyKeyReturnsEmptyWhenNoOrderRecordedForKey() {
        when(orderRepository.findOrderIdByIdempotencyKey("key-1")).thenReturn(Optional.empty());

        assertTrue(ordersService.findByIdempotencyKey("key-1").isEmpty());
    }

    @Test
    void findByIdempotencyKeyLoadsTheOrderTheKeyPointsTo() {
        Order order = Order.create(CustomerId.newId());
        when(orderRepository.findOrderIdByIdempotencyKey("key-1")).thenReturn(Optional.of(order.orderId()));
        when(orderRepository.findById(order.orderId())).thenReturn(Optional.of(order));

        assertSame(order, ordersService.findByIdempotencyKey("key-1").orElseThrow());
    }

    @Test
    void recordIdempotencyKeyDelegatesToRepository() {
        OrderId orderId = OrderId.newId();

        ordersService.recordIdempotencyKey("key-1", orderId);

        verify(orderRepository).recordIdempotencyKey("key-1", orderId);
    }
}
