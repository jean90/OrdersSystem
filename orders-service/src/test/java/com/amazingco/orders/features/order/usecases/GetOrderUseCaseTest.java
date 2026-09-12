package com.amazingco.orders.features.order.usecases;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderNotFoundException;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.order.OrdersService;
import com.amazingco.orders.features.order.dtos.GetOrderCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderUseCaseTest {

    @Mock
    private OrdersService ordersService;

    @Test
    void delegatesToServiceFindById() {
        GetOrderUseCase useCase = new GetOrderUseCase(ordersService);
        Order order = Order.create(CustomerId.newId());
        when(ordersService.findById(order.orderId())).thenReturn(order);

        Order result = useCase.execute(new GetOrderCommand("trace-1", order.orderId()));

        assertSame(order, result);
    }

    @Test
    void propagatesOrderNotFoundExceptionFromService() {
        GetOrderUseCase useCase = new GetOrderUseCase(ordersService);
        OrderId orderId = OrderId.newId();
        when(ordersService.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(new GetOrderCommand("trace-1", orderId)));
    }
}
