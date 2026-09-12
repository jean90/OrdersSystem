package com.amazingco.orders.features.order.usecases;

import com.amazingco.core.order.Order;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.orders.features.order.OrdersService;
import com.amazingco.orders.features.order.dtos.GetOrderCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOrderUseCase implements UseCase<GetOrderCommand, Order> {

    private final OrdersService ordersService;

    public GetOrderUseCase(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @Override
    @Transactional(readOnly = true)
    public Order execute(GetOrderCommand command) {
        return ordersService.findById(command.orderId());
    }
}
