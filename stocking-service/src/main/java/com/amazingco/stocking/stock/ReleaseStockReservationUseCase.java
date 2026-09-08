package com.amazingco.stocking.stock;

import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.stock.StockReservationNotFoundException;
import com.amazingco.core.usecase.UseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The saga's compensation step — must be as reliable as the happy path. Idempotent:
 * {@link StockReservation#release()} no-ops if already RELEASED, and this only touches
 * {@link StockService} on the transition, never on replay.
 */
@Service
public class ReleaseStockReservationUseCase implements UseCase<ReleaseStockReservationCommand, StockReservation> {

    private final StockService stockService;
    private final StockReservationsService stockReservationsService;

    public ReleaseStockReservationUseCase(StockService stockService,
                                           StockReservationsService stockReservationsService) {
        this.stockService = stockService;
        this.stockReservationsService = stockReservationsService;
    }

    @Override
    @Transactional
    public StockReservation execute(ReleaseStockReservationCommand command) {
        StockReservation reservation = stockReservationsService.findByOrderIdAndSku(command.orderId(), command.sku())
                .orElseThrow(() -> new StockReservationNotFoundException(command.orderId(), command.sku()));

        boolean alreadyReleased = reservation.status() == ReservationStatus.RELEASED;
        reservation.release();
        if (!alreadyReleased) {
            stockService.releaseReservation(command.sku(), reservation.quantity());
        }
        return stockReservationsService.save(reservation);
    }
}
