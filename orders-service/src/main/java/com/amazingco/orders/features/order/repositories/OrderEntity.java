package com.amazingco.orders.features.order.repositories;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderLine;
import com.amazingco.core.order.OrderStatus;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

/**
 * Persistence-mapped row for the {@code orders} table (see {@code V1__create_order_tables.sql}),
 * with its {@code order_line} children eagerly loaded as one JPA aggregate — {@link Order} is a
 * true DDD aggregate root (whole-aggregate consistency, not the single-row atomic-update style
 * {@code stocking-service} uses for {@code Stock}), so partial loading isn't acceptable here.
 * Core's {@link Order} stays framework-light (no JPA annotations); this is the JPA-facing shape,
 * translated to/from the domain aggregate at the repository boundary.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected OrderEntity() {
        // JPA
    }

    private OrderEntity(UUID id, UUID customerId, String status, BigDecimal totalAmount, String currency,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    static OrderEntity fromDomain(Order order) {
        Money total = order.totalAmount();
        OrderEntity entity = new OrderEntity(
                order.orderId().value(),
                order.customerId().value(),
                order.status().name(),
                total != null ? total.amount() : null,
                total != null ? total.currency().getCurrencyCode() : null,
                order.createdAt(),
                order.updatedAt());
        for (OrderLine line : order.lines()) {
            entity.lines.add(OrderLineEntity.fromDomain(line, entity));
        }
        return entity;
    }

    Order toDomain() {
        List<OrderLine> domainLines = lines.stream().map(OrderLineEntity::toDomain).toList();
        Money total = totalAmount != null ? new Money(totalAmount, Currency.getInstance(currency)) : null;
        return Order.reconstitute(new OrderId(id), new CustomerId(customerId), domainLines,
                OrderStatus.valueOf(status), total, createdAt, updatedAt);
    }
}
