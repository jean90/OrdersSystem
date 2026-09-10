package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Stock;
import com.amazingco.core.usecase.UseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetStockUseCase implements UseCase<GetStockCommand, Stock> {

    private final StockService stockService;

    public GetStockUseCase(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    @Transactional(readOnly = true)
    public Stock execute(GetStockCommand command) {
        return stockService.findBySku(command.sku());
    }
}
