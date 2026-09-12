package com.amazingco.orders.features.order.web;

import com.amazingco.core.correlation.TraceIds;
import com.amazingco.core.order.Order;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.features.order.dtos.CreateOrderCommand;
import com.amazingco.orders.features.order.dtos.CreateOrderRequest;
import com.amazingco.orders.features.order.dtos.GetOrderCommand;
import com.amazingco.orders.features.order.dtos.OrderLineCommand;
import com.amazingco.orders.features.order.dtos.OrderLineRequest;
import com.amazingco.orders.features.order.dtos.OrderResponse;
import com.amazingco.orders.features.order.usecases.CreateOrderUseCase;
import com.amazingco.orders.features.order.usecases.GetOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        if (idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(IDEMPOTENCY_KEY_HEADER + " header must not be blank");
        }
        String traceId = TraceIds.resolve(traceIdHeader);
        List<OrderLineCommand> lines = request.lines().stream().map(this::toLineCommand).toList();
        Order order = createOrderUseCase.execute(new CreateOrderCommand(traceId,
                CustomerId.of(request.customerId()), lines, idempotencyKey));
        return ResponseEntity.created(URI.create("/api/orders/" + order.orderId().value()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
                                  @PathVariable("id") UUID id) {
        Order order = getOrderUseCase.execute(new GetOrderCommand(TraceIds.resolve(traceIdHeader), new OrderId(id)));
        return OrderResponse.from(order);
    }

    private OrderLineCommand toLineCommand(OrderLineRequest line) {
        return new OrderLineCommand(new Sku(line.sku()), Quantity.of(line.quantity()),
                new Money(line.unitPrice(), Currency.getInstance(line.currency())));
    }
}
