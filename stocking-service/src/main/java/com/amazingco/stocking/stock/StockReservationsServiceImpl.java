package com.amazingco.stocking.stock;

import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StockReservationsServiceImpl implements StockReservationsService {

    private final StockReservationsRepository stockReservationsRepository;

    public StockReservationsServiceImpl(StockReservationsRepository stockReservationsRepository) {
        this.stockReservationsRepository = stockReservationsRepository;
    }

    @Override
    public Optional<StockReservation> findByOrderIdAndSku(OrderId orderId, Sku sku) {
        return stockReservationsRepository.findByOrderIdAndSku(orderId, sku);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        return stockReservationsRepository.save(reservation);
    }
}
