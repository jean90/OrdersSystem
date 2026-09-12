package com.amazingco.orders.features.order.repositories;

import com.amazingco.core.order.OrderLine;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

/**
 * Persistence-mapped row for the {@code order_line} table (see
 * {@code V1__create_order_tables.sql}). Core's {@link OrderLine} stays framework-light (no JPA
 * annotations), so this is the JPA-facing shape, translated to/from the domain object at the
 * repository boundary. A plain mutable class rather than a record, per convention (JPA entities
 * need a no-arg constructor and Hibernate-managed fields; records are reserved for DTOs/events).
 */
@Entity
@Table(name = "order_line")
public class OrderLineEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    protected OrderLineEntity() {
        // JPA
    }

    private OrderLineEntity(UUID id, OrderEntity order, String sku, int quantity, BigDecimal unitPrice,
                             String currency) {
        this.id = id;
        this.order = order;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }

    static OrderLineEntity fromDomain(OrderLine line, OrderEntity order) {
        return new OrderLineEntity(UUID.randomUUID(), order, line.sku().value(), line.quantity().value(),
                line.unitPrice().amount(), line.unitPrice().currency().getCurrencyCode());
    }

    OrderLine toDomain() {
        return OrderLine.of(new Sku(sku), Quantity.of(quantity), new Money(unitPrice, Currency.getInstance(currency)));
    }
}
