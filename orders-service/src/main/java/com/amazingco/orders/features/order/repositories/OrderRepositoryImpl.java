package com.amazingco.orders.features.order.repositories;

import com.amazingco.core.order.Order;
import com.amazingco.core.valueobject.OrderId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;
    private final SpringDataOrderIdempotencyKeyRepository springDataOrderIdempotencyKeyRepository;

    public OrderRepositoryImpl(SpringDataOrderRepository springDataOrderRepository,
                                SpringDataOrderIdempotencyKeyRepository springDataOrderIdempotencyKeyRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
        this.springDataOrderIdempotencyKeyRepository = springDataOrderIdempotencyKeyRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity saved = springDataOrderRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId orderId) {
        return springDataOrderRepository.findById(orderId.value()).map(OrderEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderId> findOrderIdByIdempotencyKey(String idempotencyKey) {
        return springDataOrderIdempotencyKeyRepository.findById(idempotencyKey).map(entity -> new OrderId(entity.orderId()));
    }

    @Override
    @Transactional
    public void recordIdempotencyKey(String idempotencyKey, OrderId orderId) {
        springDataOrderIdempotencyKeyRepository.insert(idempotencyKey, orderId.value(), Instant.now());
    }
}
