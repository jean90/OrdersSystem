package com.amazingco.stocking.features.product.dtos;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.Sku;

public final class DiscontinueProductCommand extends Command {

    private final Sku sku;

    public DiscontinueProductCommand(String traceId, Sku sku) {
        super(traceId);
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
