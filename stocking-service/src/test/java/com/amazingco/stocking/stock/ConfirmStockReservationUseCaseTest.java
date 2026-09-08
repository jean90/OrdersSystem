package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.stock.StockReservationNotFoundException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmStockReservationUseCaseTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");
    private static final Quantity QUANTITY = Quantity.of(3);

    @Mock
    private StockService stockService;

    @Mock
    private StockReservationsService stockReservationsService;

    @Test
    void throwsWhenReservationNotFound() {
        ConfirmStockReservationUseCase useCase =
                new ConfirmStockReservationUseCase(stockService, stockReservationsService);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.empty());

        assertThrows(StockReservationNotFoundException.class,
                () -> useCase.execute(new ConfirmStockReservationCommand("trace-1", ORDER_ID, SKU)));

        verify(stockService, never()).confirmReservation(SKU, QUANTITY);
    }

    @Test
    void confirmsReservationAndDecrementsReservedOnFirstConfirm() {
        ConfirmStockReservationUseCase useCase =
                new ConfirmStockReservationUseCase(stockService, stockReservationsService);
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, QUANTITY);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(reservation));
        when(stockReservationsService.save(reservation)).thenReturn(reservation);

        StockReservation result = useCase.execute(new ConfirmStockReservationCommand("trace-1", ORDER_ID, SKU));

        assertEquals(ReservationStatus.CONFIRMED, result.status());
        verify(stockService).confirmReservation(SKU, QUANTITY);
        verify(stockReservationsService).save(reservation);
    }

    @Test
    void replayDoesNotDecrementReservedAgain() {
        ConfirmStockReservationUseCase useCase =
                new ConfirmStockReservationUseCase(stockService, stockReservationsService);
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, QUANTITY);
        reservation.confirm();
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(reservation));
        when(stockReservationsService.save(reservation)).thenReturn(reservation);

        useCase.execute(new ConfirmStockReservationCommand("trace-1", ORDER_ID, SKU));

        verify(stockService, never()).confirmReservation(SKU, QUANTITY);
    }
}
