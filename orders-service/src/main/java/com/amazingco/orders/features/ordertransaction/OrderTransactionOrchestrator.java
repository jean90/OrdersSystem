package com.amazingco.orders.features.ordertransaction;

import com.amazingco.core.event.ReserveStockForOrderCommand;
import com.amazingco.core.event.StockReservationFailedEvent;
import com.amazingco.core.event.StockReservationLineItem;
import com.amazingco.core.event.StockReservedEvent;
import com.amazingco.core.event.Topics;
import com.amazingco.core.order.Order;
import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.order.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Drives the order-transaction saga: starts it and dispatches the reserve-stock command when an
 * order is created, then reacts to the outcome events {@code stocking-service} publishes back.
 * This slice only ever reaches {@code STOCK_RESERVED} or {@code CANCELLED} — see
 * {@link OrderTransaction}'s Javadoc for why {@code PAID}/{@code CONFIRMED} aren't driven yet.
 */
@Component
public class OrderTransactionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderTransactionOrchestrator.class);

    private final OrderTransactionsService orderTransactionsService;
    private final OrdersService ordersService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderTransactionOrchestrator(OrderTransactionsService orderTransactionsService, OrdersService ordersService,
                                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderTransactionsService = orderTransactionsService;
        this.ordersService = ordersService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Called by {@code CreateOrderUseCase} right after a freshly-created order is persisted
     * (never on an idempotent replay). Note: this runs inside the caller's already-open
     * {@code @Transactional} method, so — by Spring's default {@code REQUIRED} propagation — the
     * {@link OrderTransaction} row commits in the same DB transaction as the order, but the
     * Kafka publish below happens <em>before</em> that transaction commits, not after. There's no
     * transactional outbox here (out of scope for this slice); a crash between the publish and
     * the commit is an accepted, undetected gap.
     */
    @Transactional
    public void start(Order order, String traceId) {
        orderTransactionsService.start(order.orderId());
        log.info("Order transaction started for order {} (traceId={})", order.orderId().value(), traceId);

        List<StockReservationLineItem> lines = order.lines().stream()
                .map(line -> new StockReservationLineItem(line.sku().value(), line.quantity().value(),
                        line.unitPrice().amount(), line.unitPrice().currency().getCurrencyCode()))
                .toList();
        kafkaTemplate.send(Topics.RESERVE_STOCK_COMMANDS, order.orderId().value().toString(),
                new ReserveStockForOrderCommand(traceId, order.orderId().value(), lines));
    }

    @KafkaListener(topics = Topics.STOCK_RESERVED_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onStockReserved(StockReservedEvent event) {
        OrderId orderId = new OrderId(event.orderId());
        OrderTransaction transaction = orderTransactionsService.findByOrderId(orderId);
        transaction.markStockReserved();
        orderTransactionsService.save(transaction);
        log.info("Order transaction for order {} reached STOCK_RESERVED (traceId={})", orderId.value(),
                event.traceId());
        // Order itself stays PENDING — nothing else modeled until a payment step exists.
    }

    @KafkaListener(topics = Topics.STOCK_RESERVATION_FAILED_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        OrderId orderId = new OrderId(event.orderId());
        OrderTransaction transaction = orderTransactionsService.findByOrderId(orderId);
        transaction.cancel();
        orderTransactionsService.save(transaction);

        Order order = ordersService.findById(orderId);
        order.cancel();
        ordersService.update(order);
        log.info("Order transaction for order {} CANCELLED: {} (traceId={})", orderId.value(), event.reason(),
                event.traceId());
    }
}
