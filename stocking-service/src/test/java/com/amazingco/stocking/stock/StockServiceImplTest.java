package com.amazingco.stocking.stock;

import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    private static final Sku SKU = new Sku("ABC-123");
    private static final Quantity QUANTITY = Quantity.of(5);

    @Mock
    private StockRepository stockRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(stockRepository);
    }

    @Test
    void reserveSucceedsWhenRepositoryDecrementsAvailable() {
        when(stockRepository.tryReserve(SKU, QUANTITY)).thenReturn(true);

        stockService.reserve(SKU, QUANTITY);

        verify(stockRepository).tryReserve(SKU, QUANTITY);
    }

    @Test
    void reserveThrowsInsufficientStockWhenRepositoryDeclines() {
        Stock stock = Stock.initial(SKU, Quantity.of(2));
        when(stockRepository.tryReserve(SKU, QUANTITY)).thenReturn(false);
        when(stockRepository.findBySku(SKU)).thenReturn(Optional.of(stock));

        InsufficientStockException exception =
                assertThrows(InsufficientStockException.class, () -> stockService.reserve(SKU, QUANTITY));

        assertEquals(QUANTITY, exception.requested());
        assertEquals(Quantity.of(2), exception.available());
    }

    @Test
    void reserveThrowsStockNotFoundWhenSkuUnknown() {
        when(stockRepository.tryReserve(SKU, QUANTITY)).thenReturn(false);
        when(stockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> stockService.reserve(SKU, QUANTITY));
    }

    @Test
    void confirmReservationDelegatesToRepository() {
        stockService.confirmReservation(SKU, QUANTITY);

        verify(stockRepository).confirmReservation(SKU, QUANTITY);
    }

    @Test
    void releaseReservationDelegatesToRepository() {
        stockService.releaseReservation(SKU, QUANTITY);

        verify(stockRepository).releaseReservation(SKU, QUANTITY);
    }

    @Test
    void findBySkuReturnsStockWhenPresent() {
        Stock stock = Stock.initial(SKU, QUANTITY);
        when(stockRepository.findBySku(SKU)).thenReturn(Optional.of(stock));

        assertSame(stock, stockService.findBySku(SKU));
    }

    @Test
    void findBySkuThrowsWhenAbsent() {
        when(stockRepository.findBySku(SKU)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> stockService.findBySku(SKU));
    }
}
