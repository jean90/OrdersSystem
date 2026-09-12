package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.stock.StockReservationNotFoundException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.StockReservationsService;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.ReleaseStockReservationCommand;
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
class ReleaseStockReservationUseCaseTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");
    private static final Quantity QUANTITY = Quantity.of(3);

    @Mock
    private StockService stockService;

    @Mock
    private StockReservationsService stockReservationsService;

    @Test
    void throwsWhenReservationNotFound() {
        ReleaseStockReservationUseCase useCase =
                new ReleaseStockReservationUseCase(stockService, stockReservationsService);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.empty());

        assertThrows(StockReservationNotFoundException.class,
                () -> useCase.execute(new ReleaseStockReservationCommand("trace-1", ORDER_ID, SKU)));

        verify(stockService, never()).releaseReservation(SKU, QUANTITY);
    }

    @Test
    void releasesReservationAndReturnsQuantityToAvailableOnFirstRelease() {
        ReleaseStockReservationUseCase useCase =
                new ReleaseStockReservationUseCase(stockService, stockReservationsService);
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, QUANTITY);
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(reservation));
        when(stockReservationsService.save(reservation)).thenReturn(reservation);

        StockReservation result = useCase.execute(new ReleaseStockReservationCommand("trace-1", ORDER_ID, SKU));

        assertEquals(ReservationStatus.RELEASED, result.status());
        verify(stockService).releaseReservation(SKU, QUANTITY);
        verify(stockReservationsService).save(reservation);
    }

    @Test
    void replayDoesNotReturnQuantityAgain() {
        ReleaseStockReservationUseCase useCase =
                new ReleaseStockReservationUseCase(stockService, stockReservationsService);
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, QUANTITY);
        reservation.release();
        when(stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(reservation));
        when(stockReservationsService.save(reservation)).thenReturn(reservation);

        useCase.execute(new ReleaseStockReservationCommand("trace-1", ORDER_ID, SKU));

        verify(stockService, never()).releaseReservation(SKU, QUANTITY);
    }
}
