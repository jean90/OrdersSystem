package com.amazingco.stocking.product;

import com.amazingco.core.usecase.Command;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;

public final class UpdateProductCommand extends Command {

    private final Sku sku;
    private final String name;
    private final String description;
    private final Money price;
    private final String category;

    public UpdateProductCommand(String traceId, Sku sku, String name, String description, Money price,
                                 String category) {
        super(traceId);
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public Sku sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public String category() {
        return category;
    }
}
