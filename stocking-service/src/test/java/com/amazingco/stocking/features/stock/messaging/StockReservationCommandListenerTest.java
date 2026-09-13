package com.amazingco.stocking.features.stock.messaging;

import com.amazingco.core.event.ReserveStockForOrderCommand;
import com.amazingco.core.event.StockReservationFailedEvent;
import com.amazingco.core.event.StockReservationLineItem;
import com.amazingco.core.event.StockReservedEvent;
import com.amazingco.core.event.Topics;
import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.dtos.ReleaseStockReservationCommand;
import com.amazingco.stocking.features.stock.dtos.ReserveStockCommand;
import com.amazingco.stocking.features.stock.usecases.ReleaseStockReservationUseCase;
import com.amazingco.stocking.features.stock.usecases.ReserveStockUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReservationCommandListenerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final Sku SKU_A = new Sku("ABC-123");
    private static final Sku SKU_B = new Sku("XYZ-789");

    @Mock
    private ReserveStockUseCase reserveStockUseCase;

    @Mock
    private ReleaseStockReservationUseCase releaseStockReservationUseCase;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private StockReservationCommandListener listener;

    @Test
    void allLinesReservedPublishesSuccessEvent() {
        listener = new StockReservationCommandListener(reserveStockUseCase, releaseStockReservationUseCase, kafkaTemplate);
        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand("trace-1", ORDER_ID, List.of(
                new StockReservationLineItem("ABC-123", 2, new BigDecimal("9.99"), "USD"),
                new StockReservationLineItem("XYZ-789", 1, new BigDecimal("5.00"), "USD")));
        when(reserveStockUseCase.execute(any(ReserveStockCommand.class)))
                .thenReturn(StockReservation.create(OrderId.of(ORDER_ID.toString()), SKU_A, Quantity.of(2)));

        listener.onReserveStockCommand(command);

        verify(releaseStockReservationUseCase, never()).execute(any());
        ArgumentCaptor<StockReservedEvent> captor = ArgumentCaptor.forClass(StockReservedEvent.class);
        verify(kafkaTemplate).send(eq(Topics.STOCK_RESERVED_EVENTS), eq(ORDER_ID.toString()), captor.capture());
        assertEquals("trace-1", captor.getValue().traceId());
        assertEquals(ORDER_ID, captor.getValue().orderId());
    }

    @Test
    void secondLineFailingReleasesTheFirstAndPublishesFailureEvent() {
        listener = new StockReservationCommandListener(reserveStockUseCase, releaseStockReservationUseCase, kafkaTemplate);
        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand("trace-1", ORDER_ID, List.of(
                new StockReservationLineItem("ABC-123", 2, new BigDecimal("9.99"), "USD"),
                new StockReservationLineItem("XYZ-789", 100, new BigDecimal("5.00"), "USD")));
        OrderId orderId = OrderId.of(ORDER_ID.toString());
        when(reserveStockUseCase.execute(argThat(cmd -> cmd != null && cmd.sku().equals(SKU_A))))
                .thenReturn(StockReservation.create(orderId, SKU_A, Quantity.of(2)));
        when(reserveStockUseCase.execute(argThat(cmd -> cmd != null && cmd.sku().equals(SKU_B))))
                .thenThrow(new InsufficientStockException(SKU_B, Quantity.of(100), Quantity.of(3)));

        listener.onReserveStockCommand(command);

        verify(releaseStockReservationUseCase)
                .execute(argThat((ReleaseStockReservationCommand cmd) -> cmd != null && cmd.sku().equals(SKU_A)));
        ArgumentCaptor<StockReservationFailedEvent> captor = ArgumentCaptor.forClass(StockReservationFailedEvent.class);
        verify(kafkaTemplate).send(eq(Topics.STOCK_RESERVATION_FAILED_EVENTS), eq(ORDER_ID.toString()), captor.capture());
        assertEquals(ORDER_ID, captor.getValue().orderId());
    }
}
