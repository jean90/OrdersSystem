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
}
