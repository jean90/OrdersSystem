package com.amazingco.orders.features.ordertransaction;

import com.amazingco.core.event.ReserveStockForOrderCommand;
import com.amazingco.core.event.StockReservationFailedEvent;
import com.amazingco.core.event.StockReservedEvent;
import com.amazingco.core.event.Topics;
import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderStatus;
import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionStatus;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.features.order.OrdersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTransactionOrchestratorTest {

    private static final CustomerId CUSTOMER_ID = CustomerId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Mock
    private OrderTransactionsService orderTransactionsService;

    @Mock
    private OrdersService ordersService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderTransactionOrchestrator orchestrator;

    private Order anOrder() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(2), Money.of("9.99", "USD"));
        return order;
    }

    @Test
    void startPersistsCreatedTransactionAndPublishesReserveCommand() {
        orchestrator = new OrderTransactionOrchestrator(orderTransactionsService, ordersService, kafkaTemplate);
        Order order = anOrder();

        orchestrator.start(order, "trace-1");

        verify(orderTransactionsService).start(order.orderId());

        ArgumentCaptor<ReserveStockForOrderCommand> captor = ArgumentCaptor.forClass(ReserveStockForOrderCommand.class);
        verify(kafkaTemplate).send(eq(Topics.RESERVE_STOCK_COMMANDS), eq(order.orderId().value().toString()),
                captor.capture());
        ReserveStockForOrderCommand command = captor.getValue();
        assertEquals("trace-1", command.traceId());
        assertEquals(order.orderId().value(), command.orderId());
        assertEquals(1, command.lines().size());
        assertEquals("ABC-123", command.lines().get(0).sku());
        assertEquals(2, command.lines().get(0).quantity());
    }

    @Test
    void onStockReservedMarksTransactionStockReserved() {
        orchestrator = new OrderTransactionOrchestrator(orderTransactionsService, ordersService, kafkaTemplate);
        OrderId orderId = OrderId.newId();
        OrderTransaction transaction = OrderTransaction.start(orderId);
        when(orderTransactionsService.findByOrderId(orderId)).thenReturn(transaction);

        orchestrator.onStockReserved(new StockReservedEvent("trace-1", orderId.value()));

        assertEquals(OrderTransactionStatus.STOCK_RESERVED, transaction.status());
        verify(orderTransactionsService).save(transaction);
    }

    @Test
    void onStockReservationFailedCancelsTransactionAndOrder() {
        orchestrator = new OrderTransactionOrchestrator(orderTransactionsService, ordersService, kafkaTemplate);
        Order order = anOrder();
        OrderId orderId = order.orderId();
        OrderTransaction transaction = OrderTransaction.start(orderId);
        when(orderTransactionsService.findByOrderId(orderId)).thenReturn(transaction);
        when(ordersService.findById(orderId)).thenReturn(order);
        when(ordersService.update(any(Order.class))).thenReturn(order);

        orchestrator.onStockReservationFailed(new StockReservationFailedEvent("trace-1", orderId.value(),
                "insufficient stock"));

        assertEquals(OrderTransactionStatus.CANCELLED, transaction.status());
        assertEquals(OrderStatus.CANCELLED, order.status());
        verify(orderTransactionsService).save(transaction);
        verify(ordersService).update(order);
    }
}
