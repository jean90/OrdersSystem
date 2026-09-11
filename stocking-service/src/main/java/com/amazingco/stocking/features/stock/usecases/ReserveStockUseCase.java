package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.stock.StockReservationsService;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.ReserveStockCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent: replaying the same (orderId, sku) returns the existing reservation
 * instead of decrementing {@link StockService} again.
 */
@Service
public class ReserveStockUseCase implements UseCase<ReserveStockCommand, StockReservation> {

    private final StockService stockService;
    private final StockReservationsService stockReservationsService;

    public ReserveStockUseCase(StockService stockService, StockReservationsService stockReservationsService) {
        this.stockService = stockService;
        this.stockReservationsService = stockReservationsService;
    }

    @Override
    @Transactional
    public StockReservation execute(ReserveStockCommand command) {
        return stockReservationsService.findByOrderIdAndSku(command.orderId(), command.sku())
                .orElseGet(() -> {
                    stockService.reserve(command.sku(), command.quantity());
                    StockReservation reservation =
                            StockReservation.create(command.orderId(), command.sku(), command.quantity());
                    return stockReservationsService.save(reservation);
                });
    }
}
