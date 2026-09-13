package com.amazingco.stocking.features.stock.messaging;

import com.amazingco.core.event.ReserveStockForOrderCommand;
import com.amazingco.core.event.StockReservationFailedEvent;
import com.amazingco.core.event.StockReservationLineItem;
import com.amazingco.core.event.StockReservedEvent;
import com.amazingco.core.event.Topics;
import com.amazingco.core.exception.DomainException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.dtos.ReleaseStockReservationCommand;
import com.amazingco.stocking.features.stock.dtos.ReserveStockCommand;
import com.amazingco.stocking.features.stock.usecases.ReleaseStockReservationUseCase;
import com.amazingco.stocking.features.stock.usecases.ReserveStockUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Consumes {@link ReserveStockForOrderCommand}, reserving every line via the already-idempotent
 * {@link ReserveStockUseCase}. On failure, self-compensates by releasing whichever lines it had
 * already reserved for this order before reporting the failure — the saga's compensation must be
 * as reliable as the happy path. No changes needed to {@code ReserveStockUseCase}/
 * {@code ReleaseStockReservationUseCase}: both are already safe to replay under at-least-once
 * delivery, including this listener's own compensation loop re-running after a mid-failure crash.
 */
@Component
public class StockReservationCommandListener {

    private static final Logger log = LoggerFactory.getLogger(StockReservationCommandListener.class);

    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockReservationUseCase releaseStockReservationUseCase;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StockReservationCommandListener(ReserveStockUseCase reserveStockUseCase,
                                            ReleaseStockReservationUseCase releaseStockReservationUseCase,
                                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseStockReservationUseCase = releaseStockReservationUseCase;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = Topics.RESERVE_STOCK_COMMANDS, groupId = "${spring.kafka.consumer.group-id}")
    public void onReserveStockCommand(ReserveStockForOrderCommand command) {
        OrderId orderId = OrderId.of(command.orderId().toString());
        List<Sku> reservedSoFar = new ArrayList<>();
        try {
            for (StockReservationLineItem line : command.lines()) {
                Sku sku = new Sku(line.sku());
                reserveStockUseCase.execute(new ReserveStockCommand(command.traceId(), orderId, sku,
                        Quantity.of(line.quantity())));
                reservedSoFar.add(sku);
            }
            log.info("Reserved all lines for order {} (traceId={})", command.orderId(), command.traceId());
            kafkaTemplate.send(Topics.STOCK_RESERVED_EVENTS, command.orderId().toString(),
                    new StockReservedEvent(command.traceId(), command.orderId()));
        } catch (DomainException e) {
            for (Sku sku : reservedSoFar) {
                releaseStockReservationUseCase.execute(
                        new ReleaseStockReservationCommand(command.traceId(), orderId, sku));
            }
            log.info("Reservation failed for order {}, released {} line(s) (traceId={}): {}",
                    command.orderId(), reservedSoFar.size(), command.traceId(), e.getMessage());
            kafkaTemplate.send(Topics.STOCK_RESERVATION_FAILED_EVENTS, command.orderId().toString(),
                    new StockReservationFailedEvent(command.traceId(), command.orderId(), e.getMessage()));
        }
    }
}
