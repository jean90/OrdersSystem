package com.amazingco.orders.features.order.usecases;

import com.amazingco.core.order.Order;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.orders.features.order.OrdersService;
import com.amazingco.orders.features.order.dtos.CreateOrderCommand;
import com.amazingco.orders.features.order.dtos.OrderLineCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Idempotent: replaying the same {@code Idempotency-Key} returns the already-created order
 * instead of creating a second one (see {@code OrderRepository.recordIdempotencyKey}'s Javadoc
 * for the accepted race-window simplification this relies on).
 */
@Service
public class CreateOrderUseCase implements UseCase<CreateOrderCommand, Order> {

    private final OrdersService ordersService;

    public CreateOrderUseCase(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @Override
    @Transactional
    public Order execute(CreateOrderCommand command) {
        Optional<Order> existing = ordersService.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return existing.get();
        }
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one line");
        }
        Order order = Order.create(command.customerId());
        for (OrderLineCommand line : command.lines()) {
            order.addLine(line.sku(), line.quantity(), line.unitPrice());
        }
        Order saved = ordersService.create(order);
        ordersService.recordIdempotencyKey(command.idempotencyKey(), saved.orderId());
        return saved;
    }
}
