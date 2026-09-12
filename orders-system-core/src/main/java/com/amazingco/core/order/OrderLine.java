package com.amazingco.core.order;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;

/**
 * One line item within an {@link Order} — a SKU, the quantity ordered, and the price it was
 * ordered at (captured at order time, independent of any later {@code Product} price change).
 * Has no identity of its own; it's part of the {@code Order} aggregate, not a separate aggregate
 * root.
 */
public class OrderLine {

    private final Sku sku;
    private final Quantity quantity;
    private final Money unitPrice;

    private OrderLine(Sku sku, Quantity quantity, Money unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static OrderLine of(Sku sku, Quantity quantity, Money unitPrice) {
        if (sku == null) {
            throw new IllegalArgumentException("sku must not be null");
        }
        if (quantity == null || quantity.value() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
        return new OrderLine(sku, quantity, unitPrice);
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity.value());
    }

    public Sku sku() {
        return sku;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Money unitPrice() {
        return unitPrice;
    }
}
