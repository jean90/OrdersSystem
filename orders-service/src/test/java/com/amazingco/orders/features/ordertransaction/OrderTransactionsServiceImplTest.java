package com.amazingco.orders.features.ordertransaction;

import com.amazingco.core.ordertransaction.OrderTransaction;
import com.amazingco.core.ordertransaction.OrderTransactionNotFoundException;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.orders.features.ordertransaction.repositories.OrderTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTransactionsServiceImplTest {

    private static final OrderId ORDER_ID = OrderId.newId();

    @Mock
    private OrderTransactionRepository orderTransactionRepository;

    private OrderTransactionsService orderTransactionsService;

    @BeforeEach
    void setUp() {
        orderTransactionsService = new OrderTransactionsServiceImpl(orderTransactionRepository);
    }

    @Test
    void startPersistsANewCreatedTransaction() {
        when(orderTransactionRepository.save(any(OrderTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderTransaction result = orderTransactionsService.start(ORDER_ID);

        ArgumentCaptor<OrderTransaction> captor = ArgumentCaptor.forClass(OrderTransaction.class);
        verify(orderTransactionRepository).save(captor.capture());
        assertEquals(ORDER_ID, captor.getValue().orderId());
        assertSame(captor.getValue(), result);
    }

    @Test
    void findByOrderIdReturnsTransactionWhenPresent() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        when(orderTransactionRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(transaction));

        assertSame(transaction, orderTransactionsService.findByOrderId(ORDER_ID));
    }

    @Test
    void findByOrderIdThrowsWhenAbsent() {
        when(orderTransactionRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderTransactionNotFoundException.class, () -> orderTransactionsService.findByOrderId(ORDER_ID));
    }

    @Test
    void saveDelegatesToRepository() {
        OrderTransaction transaction = OrderTransaction.start(ORDER_ID);
        when(orderTransactionRepository.save(transaction)).thenReturn(transaction);

        OrderTransaction result = orderTransactionsService.save(transaction);

        assertSame(transaction, result);
    }
}
