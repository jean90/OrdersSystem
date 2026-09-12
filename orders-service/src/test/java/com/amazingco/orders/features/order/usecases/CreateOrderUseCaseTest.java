package com.amazingco.orders.features.order.usecases;

import com.amazingco.core.order.Order;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.features.order.OrdersService;
import com.amazingco.orders.features.order.dtos.CreateOrderCommand;
import com.amazingco.orders.features.order.dtos.OrderLineCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    private static final CustomerId CUSTOMER_ID = CustomerId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Mock
    private OrdersService ordersService;

    @Test
    void buildsOrderFromCommandLinesAndDelegatesToService() {
        CreateOrderUseCase useCase = new CreateOrderUseCase(ordersService);
        CreateOrderCommand command = new CreateOrderCommand("trace-1", CUSTOMER_ID,
                List.of(new OrderLineCommand(SKU, Quantity.of(2), Money.of("9.99", "USD"))));

        Order saved = Order.create(CUSTOMER_ID);
        when(ordersService.create(any(Order.class))).thenReturn(saved);

        Order result = useCase.execute(command);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(ordersService).create(captor.capture());
        Order passed = captor.getValue();
        assertEquals(CUSTOMER_ID, passed.customerId());
        assertEquals(1, passed.lines().size());
        assertEquals(Money.of("19.98", "USD"), passed.totalAmount());
        assertSame(saved, result);
    }

    @Test
    void rejectsAnEmptyLineList() {
        CreateOrderUseCase useCase = new CreateOrderUseCase(ordersService);
        CreateOrderCommand command = new CreateOrderCommand("trace-1", CUSTOMER_ID, List.of());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }
}
