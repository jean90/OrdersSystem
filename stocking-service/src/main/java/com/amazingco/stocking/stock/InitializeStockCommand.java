package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.Sku;

public final class InitializeStockCommand extends Command {

    private final Sku sku;
    private final Quantity initialAvailable;

    public InitializeStockCommand(String traceId, Sku sku, Quantity initialAvailable) {
        super(traceId);
        this.sku = sku;
        this.initialAvailable = initialAvailable;
    }

    public Sku sku() {
        return sku;
    }

    public Quantity initialAvailable() {
        return initialAvailable;
    }
}
