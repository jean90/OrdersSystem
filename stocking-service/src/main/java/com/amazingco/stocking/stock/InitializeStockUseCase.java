package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Stock;
import com.amazingco.core.usecase.UseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InitializeStockUseCase implements UseCase<InitializeStockCommand, Stock> {

    private final StockService stockService;

    public InitializeStockUseCase(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    @Transactional
    public Stock execute(InitializeStockCommand command) {
        Stock stock = Stock.initial(command.sku(), command.initialAvailable());
        stockService.initialize(stock.sku(), stock.available());
        return stock;
    }
}
