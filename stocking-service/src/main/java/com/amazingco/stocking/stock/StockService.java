package com.amazingco.stocking.stock;

import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.valueobject.Sku;

/**
 * Aggregate service port for {@link Stock}. Deals only in the quantity aggregate —
 * coordinating it with the {@link com.amazingco.core.stock.StockReservation} ledger is
 * the use case's job (see {@link com.amazingco.core.usecase.UseCase}).
 */
public interface StockService {

    /**
     * Throws {@link InsufficientStockException} if not enough is available, or
     * {@link StockNotFoundException} if the SKU is unknown.
     */
    void reserve(Sku sku, Quantity quantity);

    /**
     * Throws {@link StockAlreadyExistsException} if a stock row already exists for this SKU.
     */
    void initialize(Sku sku, Quantity initialAvailable);

    void confirmReservation(Sku sku, Quantity quantity);

    void releaseReservation(Sku sku, Quantity quantity);

    /**
     * Throws {@link StockNotFoundException} if no stock is registered for this SKU.
     */
    Stock findBySku(Sku sku);
}
