package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.Stock;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.InitializeStockCommand;
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
