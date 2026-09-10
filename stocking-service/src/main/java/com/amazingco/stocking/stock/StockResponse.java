package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Stock;

public record StockResponse(String sku, int available, int reserved) {

    static StockResponse from(Stock stock) {
        return new StockResponse(stock.sku().value(), stock.available().value(), stock.reserved().value());
    }
}
