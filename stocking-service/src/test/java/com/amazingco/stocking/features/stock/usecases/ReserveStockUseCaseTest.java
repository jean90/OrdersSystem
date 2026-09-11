package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.StockReservationsService;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.ReserveStockCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveStockUseCaseTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");
    private static final Quantity QUANTITY = Quantity.of(3);

    @Mock
    private StockService stockService;

    @Mock
    private StockReservationsService stockReservationsService;

    @Test
    void reservesStockAndSavesNewReservationWhenNoneExists() {
        ReserveStockUseCase useCase = new ReserveStockUseCase(stockService, stockReservationsService);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.empty());
        when(stockReservationsService.save(any(StockReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockReservation result = useCase.execute(new ReserveStockCommand("trace-1", ORDER_ID, SKU, QUANTITY));

        verify(stockService).reserve(SKU, QUANTITY);
        ArgumentCaptor<StockReservation> captor = ArgumentCaptor.forClass(StockReservation.class);
        verify(stockReservationsService).save(captor.capture());
        StockReservation saved = captor.getValue();
        assertEquals(ORDER_ID, saved.orderId());
        assertEquals(SKU, saved.sku());
        assertEquals(QUANTITY, saved.quantity());
        assertEquals(ReservationStatus.RESERVED, saved.status());
        assertSame(saved, result);
    }

    @Test
    void replayReturnsExistingReservationWithoutReReserving() {
        ReserveStockUseCase useCase = new ReserveStockUseCase(stockService, stockReservationsService);
        StockReservation existing = StockReservation.create(ORDER_ID, SKU, QUANTITY);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(existing));

        StockReservation result = useCase.execute(new ReserveStockCommand("trace-1", ORDER_ID, SKU, QUANTITY));

        assertSame(existing, result);
        verify(stockService, never()).reserve(SKU, QUANTITY);
        verify(stockReservationsService, never()).save(existing);
    }
}
