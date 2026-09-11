package com.amazingco.stocking.features.product.dtos;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.Sku;

public final class GetProductCommand extends Command {

    private final Sku sku;

    public GetProductCommand(String traceId, Sku sku) {
        super(traceId);
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
