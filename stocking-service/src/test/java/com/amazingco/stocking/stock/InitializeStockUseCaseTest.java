package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InitializeStockUseCaseTest {

    @Mock
    private StockService stockService;

    @Test
    void initializesStockAndReturnsBuiltAggregate() {
        InitializeStockUseCase useCase = new InitializeStockUseCase(stockService);
        Sku sku = new Sku("ABC-123");
        Quantity initialAvailable = Quantity.of(20);
        InitializeStockCommand command = new InitializeStockCommand("trace-1", sku, initialAvailable);

        Stock result = useCase.execute(command);

        verify(stockService).initialize(sku, initialAvailable);
        assertEquals(sku, result.sku());
        assertEquals(initialAvailable, result.available());
        assertEquals(Quantity.ZERO, result.reserved());
    }

    @Test
    void propagatesStockAlreadyExistsExceptionFromService() {
        InitializeStockUseCase useCase = new InitializeStockUseCase(stockService);
        Sku sku = new Sku("ABC-123");
        Quantity initialAvailable = Quantity.of(20);
        InitializeStockCommand command = new InitializeStockCommand("trace-1", sku, initialAvailable);

        doThrow(new StockAlreadyExistsException(sku)).when(stockService).initialize(sku, initialAvailable);

        assertThrows(StockAlreadyExistsException.class, () -> useCase.execute(command));
    }
}
