package com.amazingco.stocking.features.stock.dtos;

import com.amazingco.core.stock.Stock;

public record StockResponse(String sku, int available, int reserved) {

    public static StockResponse from(Stock stock) {
        return new StockResponse(stock.sku().value(), stock.available().value(), stock.reserved().value());
    }
}
