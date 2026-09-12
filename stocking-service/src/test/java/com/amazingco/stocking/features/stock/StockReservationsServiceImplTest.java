package com.amazingco.stocking.features.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.repositories.StockReservationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReservationsServiceImplTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Mock
    private StockReservationsRepository stockReservationsRepository;

    private StockReservationsService stockReservationsService;

    @BeforeEach
    void setUp() {
        stockReservationsService = new StockReservationsServiceImpl(stockReservationsRepository);
    }

    @Test
    void findByOrderIdAndSkuDelegatesToRepository() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(3));
        when(stockReservationsRepository.findByOrderIdAndSku(ORDER_ID, SKU)).thenReturn(Optional.of(reservation));

        assertEquals(Optional.of(reservation), stockReservationsService.findByOrderIdAndSku(ORDER_ID, SKU));
    }

    @Test
    void saveDelegatesToRepository() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(3));
        when(stockReservationsRepository.save(reservation)).thenReturn(reservation);

        assertSame(reservation, stockReservationsService.save(reservation));
    }
}
