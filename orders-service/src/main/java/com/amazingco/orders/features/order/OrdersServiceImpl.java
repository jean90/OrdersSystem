package com.amazingco.orders.features.order;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderNotFoundException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.order.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrderRepository orderRepository;

    public OrdersServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order findById(OrderId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
