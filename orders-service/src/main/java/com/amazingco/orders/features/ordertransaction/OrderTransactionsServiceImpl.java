package com.amazingco.orders.features.ordertransaction;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionNotFoundException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.ordertransaction.repositories.OrderTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderTransactionsServiceImpl implements OrderTransactionsService {

    private final OrderTransactionRepository orderTransactionRepository;

    public OrderTransactionsServiceImpl(OrderTransactionRepository orderTransactionRepository) {
        this.orderTransactionRepository = orderTransactionRepository;
    }

    @Override
    public OrderTransaction start(OrderId orderId) {
        return orderTransactionRepository.save(OrderTransaction.start(orderId));
    }

    @Override
    public OrderTransaction findByOrderId(OrderId orderId) {
        return orderTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderTransactionNotFoundException(orderId));
    }

    @Override
    public OrderTransaction save(OrderTransaction orderTransaction) {
        return orderTransactionRepository.save(orderTransaction);
    }
}
