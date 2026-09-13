package com.amazingco.orders.features.ordertransaction.repositories;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.valueobject.OrderId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class OrderTransactionRepositoryImpl implements OrderTransactionRepository {

    private final SpringDataOrderTransactionRepository springDataOrderTransactionRepository;

    public OrderTransactionRepositoryImpl(SpringDataOrderTransactionRepository springDataOrderTransactionRepository) {
        this.springDataOrderTransactionRepository = springDataOrderTransactionRepository;
    }

    @Override
    @Transactional
    public OrderTransaction save(OrderTransaction orderTransaction) {
        OrderTransactionEntity entity = OrderTransactionEntity.fromDomain(orderTransaction);
        OrderTransactionEntity saved = springDataOrderTransactionRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderTransaction> findByOrderId(OrderId orderId) {
        return springDataOrderTransactionRepository.findById(orderId.value()).map(OrderTransactionEntity::toDomain);
    }
}
