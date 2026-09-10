package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.valueobject.Sku;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StockRepositoryImpl implements StockRepository {

    private final SpringDataStockRepository springDataStockRepository;

    public StockRepositoryImpl(SpringDataStockRepository springDataStockRepository) {
        this.springDataStockRepository = springDataStockRepository;
    }

    @Override
    public boolean tryReserve(Sku sku, Quantity quantity) {
        return springDataStockRepository.tryReserve(sku.value(), quantity.value()) > 0;
    }

    @Override
    public boolean tryInitialize(Sku sku, Quantity initialAvailable) {
        return springDataStockRepository.tryInitialize(sku.value(), initialAvailable.value()) > 0;
    }

    @Override
    public void confirmReservation(Sku sku, Quantity quantity) {
        springDataStockRepository.confirmReservation(sku.value(), quantity.value());
    }

    @Override
    public void releaseReservation(Sku sku, Quantity quantity) {
        springDataStockRepository.releaseReservation(sku.value(), quantity.value());
    }

    @Override
    public Optional<Stock> findBySku(Sku sku) {
        return springDataStockRepository.findById(sku.value()).map(StockEntity::toDomain);
    }
}
