package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.stock.StockReservationNotFoundException;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.stock.StockReservationsService;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.ConfirmStockReservationCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent: {@link StockReservation#confirm()} no-ops if already CONFIRMED, and this
 * only touches {@link StockService} on the RESERVED→CONFIRMED transition, never on
 * replay — otherwise a duplicate delivery would double-decrement {@code reserved}.
 */
@Service
public class ConfirmStockReservationUseCase implements UseCase<ConfirmStockReservationCommand, StockReservation> {

    private final StockService stockService;
    private final StockReservationsService stockReservationsService;

    public ConfirmStockReservationUseCase(StockService stockService,
                                           StockReservationsService stockReservationsService) {
        this.stockService = stockService;
        this.stockReservationsService = stockReservationsService;
    }

    @Override
    @Transactional
    public StockReservation execute(ConfirmStockReservationCommand command) {
        StockReservation reservation = stockReservationsService.findByOrderIdAndSku(command.orderId(), command.sku())
                .orElseThrow(() -> new StockReservationNotFoundException(command.orderId(), command.sku()));

        boolean alreadyConfirmed = reservation.status() == ReservationStatus.CONFIRMED;
        reservation.confirm();
        if (!alreadyConfirmed) {
            stockService.confirmReservation(command.sku(), reservation.quantity());
        }
        return stockReservationsService.save(reservation);
    }
}
