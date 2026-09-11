package com.amazingco.stocking.features.stock.repositories;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.valueobject.Sku;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Persistence-mapped row for the {@code stock} table (see
 * {@code V2__create_stock_tables.sql}). Only used to read the current snapshot — writes
 * go through {@link SpringDataStockRepository}'s atomic conditional-update queries, never
 * a load/mutate/save on this entity (see the concurrency architecture note).
 */
@Table("stock")
public record StockEntity(@Id String sku, int available, int reserved) {

    Stock toDomain() {
        return Stock.reconstitute(new Sku(sku), Quantity.of(available), Quantity.of(reserved));
    }
}
