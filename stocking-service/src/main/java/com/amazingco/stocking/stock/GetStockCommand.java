package com.amazingco.stocking.stock;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.Sku;

public final class GetStockCommand extends Command {

    private final Sku sku;

    public GetStockCommand(String traceId, Sku sku) {
        super(traceId);
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
