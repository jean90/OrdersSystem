package com.amazingco.stocking.features.stock.usecases;

import com.amazingco.core.stock.Stock;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.stock.StockService;
import com.amazingco.stocking.features.stock.dtos.GetStockCommand;
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
