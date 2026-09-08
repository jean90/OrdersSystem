package com.amazingco.stocking.stock;

import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.valueobject.Sku;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    public StockServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void reserve(Sku sku, Quantity quantity) {
        boolean reserved = stockRepository.tryReserve(sku, quantity);
        if (!reserved) {
            Stock stock = findBySku(sku);
            throw new InsufficientStockException(sku, quantity, stock.available());
        }
    }

    @Override
    public void confirmReservation(Sku sku, Quantity quantity) {
        stockRepository.confirmReservation(sku, quantity);
    }

    @Override
    public void releaseReservation(Sku sku, Quantity quantity) {
        stockRepository.releaseReservation(sku, quantity);
    }

    @Override
    public Stock findBySku(Sku sku) {
        return stockRepository.findBySku(sku)
                .orElseThrow(() -> new StockNotFoundException(sku));
    }
}
